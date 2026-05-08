package com.example.mayarpg;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mayarpg.network.ApiClient;
import com.example.mayarpg.network.services.AgendaService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment para agendamento de consultas com calendário interativo
 * Usa a biblioteca Kizitonwose Calendar para exibir um calendário mensal
 * onde o usuário pode selecionar um dia e depois escolher um horário
 */
public class ScheduleFragment extends Fragment {

    // Armazena os horários selecionados para cada data (chave: "YYYY-MM-DD", valor: "HH:MM")
    private final Map<String, String> selectedHourByDate = new HashMap<>();
    
    // Lista de botões de horário para facilitar a atualização visual
    private final List<Button> timeButtons = new ArrayList<>();

    // Views principais
    private CalendarView calendarView;
    private TextView tvMonth;
    private TextView tvYear;
    private TextView tvSelectedDate;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private Button btnConfirmarAgendamento;

    // Data atualmente selecionada pelo usuário
    private LocalDate selectedDate = LocalDate.now();
    
    // Mês atualmente exibido no calendário
    private YearMonth currentMonth = YearMonth.now();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa as views
        calendarView = view.findViewById(R.id.calendarView);
        tvMonth = view.findViewById(R.id.tvMonth);
        tvYear = view.findViewById(R.id.tvYear);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        btnConfirmarAgendamento = view.findViewById(R.id.btnConfirmarAgendamento);

        // Configura os botões de horário
        setupTimeButtons(view);
        setupConfirmButton();

        // Configura o calendário
        setupCalendar();

        // Configura os botões de navegação de mês
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.minusMonths(1);
            calendarView.scrollToMonth(currentMonth);
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth = currentMonth.plusMonths(1);
            calendarView.scrollToMonth(currentMonth);
        });

        // Atualiza o label da data selecionada
        updateSelectedDateLabel();

        // Atualiza os botões de horário para refletir a seleção atual
        refreshTimeSelection();

        SessionBookingFirestore.fetchAndCacheLocal(requireContext(), () -> {
            if (isAdded()) {
                restoreBookingFromPreferences();
            }
        });
    }

    private void setupConfirmButton() {
        btnConfirmarAgendamento.setOnClickListener(v -> showConfirmBookingDialog());
    }

    private void updateConfirmButtonState() {
        String key = getDateKey(selectedDate);
        String hour = selectedHourByDate.get(key);
        boolean ready = hour != null && !hour.isEmpty();
        btnConfirmarAgendamento.setEnabled(ready);
    }

    private void showConfirmBookingDialog() {
        if (getContext() == null) {
            return;
        }
        String key = getDateKey(selectedDate);
        String time = selectedHourByDate.get(key);
        if (time == null || time.isEmpty()) {
            return;
        }
        String dateLabel = formatSelectedDateShort();
        String msg = getString(R.string.dialog_confirmar_agendamento_msg, dateLabel, time);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_confirmar_agendamento_titulo)
                .setMessage(msg)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.confirmar, (dialog, which) -> confirmAndPersistBooking(key, time, dateLabel))
                .show();
    }

    /**
     * Linha curta tipo "Ter, 19/05" (sem hora).
     */
    private String formatSelectedDateShort() {
        String[] weekPt = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "S\u00e1b"};
        int dayOfWeek = selectedDate.getDayOfWeek().getValue();
        int weekIndex = dayOfWeek % 7;
        String weekDay = weekPt[weekIndex];
        return String.format(Locale.getDefault(),
                "%s, %02d/%02d",
                weekDay,
                selectedDate.getDayOfMonth(),
                selectedDate.getMonthValue());
    }

    private void confirmAndPersistBooking(String dateIso, String timeText, String dateLabel) {
        SessionBookingPreferences.save(requireContext(), dateIso, timeText);
        SessionBookingFirestore.syncToCloud(requireContext(), dateIso, timeText);
        AppointmentHistoryStore.append(requireContext(), dateIso, timeText);
        // Envia agendamento para API central sem interromper o fluxo local.
        syncBookingToBackend(dateIso, timeText);

        Toast.makeText(requireContext(), R.string.agendamento_confirmado_toast, Toast.LENGTH_SHORT).show();

        StringBuilder msg = new StringBuilder(getString(R.string.whatsapp_msg_confirmacao_agendamento,
                dateLabel, timeText, dateIso));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            msg.append("\nConta: ").append(user.getEmail().trim());
        }
        openWhatsappParaMaya(msg.toString());
    }

    private void syncBookingToBackend(String dateIso, String timeText) {
        String nome = resolveCurrentUserDisplayName();
        AgendaService.CriarAgendamentoRequest body =
                new AgendaService.CriarAgendamentoRequest(dateIso, timeText, nome, "Criado pelo app Android");

        ApiClient.agendaService(requireContext()).criarAgendamento(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!response.isSuccessful() && isAdded()) {
                    Toast.makeText(requireContext(), "Agendamento local salvo, API retornou erro.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Sem conexao com API para sincronizar agendamento.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String resolveCurrentUserDisplayName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return "Paciente";
        }
        if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            return user.getDisplayName().trim();
        }
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            return user.getEmail().trim();
        }
        return "Paciente";
    }

    private void openWhatsappParaMaya(String texto) {
        if (getContext() == null) {
            return;
        }
        String raw = getString(R.string.whatsapp_maya_numero).trim().replaceAll("\\D", "");
        if (raw.isEmpty()) {
            Toast.makeText(requireContext(), R.string.numero_whatsapp_nao_configurado, Toast.LENGTH_LONG).show();
            return;
        }
        String url = "https://wa.me/" + raw + "?text=" + Uri.encode(texto);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    /**
     * Recupera data/horario ja guardados (proxima sessao) e reflete no calendario.
     */
    private void restoreBookingFromPreferences() {
        if (getContext() == null) {
            return;
        }
        String iso = SessionBookingPreferences.getDateIso(requireContext());
        String time = SessionBookingPreferences.getTime(requireContext());
        if (iso == null || time == null || iso.isEmpty() || time.isEmpty()) {
            return;
        }
        try {
            LocalDate saved = LocalDate.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate previous = selectedDate;
            selectedDate = saved;
            selectedHourByDate.put(iso, time);
            currentMonth = YearMonth.from(saved);
            calendarView.scrollToMonth(currentMonth);
            calendarView.notifyDateChanged(previous);
            calendarView.notifyDateChanged(selectedDate);
            updateMonthYearLabelsFromYearMonth(currentMonth);
            updateSelectedDateLabel();
            refreshTimeSelection();
        } catch (Exception ignored) {
        }
    }

    private void updateMonthYearLabelsFromYearMonth(YearMonth ym) {
        String[] months = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
                "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        int monthIndex = ym.getMonthValue() - 1;
        tvMonth.setText(months[monthIndex]);
        tvYear.setText(String.valueOf(ym.getYear()));
    }

    /**
     * Configura o calendário com a biblioteca Kizitonwose
     * Define como cada dia deve ser renderizado e o que acontece ao clicar
     */
    private void setupCalendar() {
        // Define como cada dia do calendário deve ser renderizado
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay calendarDay) {
                container.day = calendarDay;
                TextView textView = container.textView;

                // Mostra o número do dia apenas se pertencer ao mês atual
                if (calendarDay.getPosition() == DayPosition.MonthDate) {
                    textView.setText(String.valueOf(calendarDay.getDate().getDayOfMonth()));
                    textView.setVisibility(View.VISIBLE);

                    // Verifica se este dia está selecionado
                    boolean isSelected = calendarDay.getDate().equals(selectedDate);

                    if (isSelected) {
                        // Dia selecionado: fundo escuro, texto branco
                        textView.setBackgroundResource(R.drawable.bg_calendar_day_selected);
                        textView.setTextColor(Color.WHITE);
                    } else {
                        // Dia normal: fundo transparente, texto preto
                        textView.setBackgroundColor(Color.TRANSPARENT);
                        textView.setTextColor(Color.BLACK);
                    }

                    // Configura o clique no dia
                    textView.setOnClickListener(v -> {
                        // Atualiza a data selecionada
                        LocalDate previousDate = selectedDate;
                        selectedDate = calendarDay.getDate();

                        // Atualiza apenas os dias afetados (otimização)
                        calendarView.notifyDateChanged(previousDate);
                        calendarView.notifyDateChanged(selectedDate);

                        // Atualiza o label e os botões de horário
                        updateSelectedDateLabel();
                        refreshTimeSelection();
                    });
                } else {
                    // Dias de outros meses ficam invisíveis
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Listener para mudanças de mês (atualiza os TextViews de mês/ano)
        calendarView.setMonthScrollListener(calendarMonth -> {
            updateMonthYearLabels(calendarMonth);
            return Unit.INSTANCE;
        });

        // Define o intervalo de meses visíveis (6 meses para trás e 12 para frente)
        YearMonth startMonth = currentMonth.minusMonths(6);
        YearMonth endMonth = currentMonth.plusMonths(12);
        
        // Configura o calendário para começar no domingo
        calendarView.setup(startMonth, endMonth, DayOfWeek.SUNDAY);
        
        // Rola para o mês atual
        calendarView.scrollToMonth(currentMonth);
    }

    /**
     * Atualiza os labels de mês e ano no topo do calendário
     */
    private void updateMonthYearLabels(CalendarMonth calendarMonth) {
        currentMonth = calendarMonth.getYearMonth();
        
        // Nomes dos meses abreviados
        String[] months = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", 
                          "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        
        int monthIndex = currentMonth.getMonthValue() - 1;
        tvMonth.setText(months[monthIndex]);
        tvYear.setText(String.valueOf(currentMonth.getYear()));
    }

    /**
     * Configura os botões de horário e seus listeners
     */
    private void setupTimeButtons(View root) {
        timeButtons.clear();
        timeButtons.add(root.findViewById(R.id.btnTime0900));
        timeButtons.add(root.findViewById(R.id.btnTime0930));
        timeButtons.add(root.findViewById(R.id.btnTime1000));
        timeButtons.add(root.findViewById(R.id.btnTime1030));
        timeButtons.add(root.findViewById(R.id.btnTime1100));
        timeButtons.add(root.findViewById(R.id.btnTime1130));

        // Configura o clique de cada botão de horário
        for (Button button : timeButtons) {
            button.setOnClickListener(v -> {
                // Armazena o horário selecionado para a data atual
                String key = getDateKey(selectedDate);
                String hourLabel = button.getText().toString();
                selectedHourByDate.put(key, hourLabel);

                // Atualiza a aparência dos botões
                refreshTimeSelection();
            });
        }
    }

    /**
     * Atualiza a aparência dos botões de horário
     * O horário selecionado fica verde, os demais ficam azuis
     */
    private void refreshTimeSelection() {
        String selectedHour = selectedHourByDate.get(getDateKey(selectedDate));
        
        for (Button button : timeButtons) {
            boolean isSelected = button.getText().toString().equals(selectedHour);
            
            // Verde se selecionado, azul caso contrário
            button.setBackgroundResource(isSelected ? R.drawable.bg_button_green : R.drawable.bg_chip_blue);
        }
        updateConfirmButtonState();
    }

    /**
     * Atualiza o TextView que mostra a data selecionada (ex: "Qua, 22/04")
     */
    private void updateSelectedDateLabel() {
        // Nomes dos dias da semana em português
        String[] weekPt = {"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        
        // Obtém o índice do dia da semana (1=Monday...7=Sunday)
        int dayOfWeek = selectedDate.getDayOfWeek().getValue();
        
        // Ajusta para o array (Domingo=0)
        int weekIndex = dayOfWeek % 7;
        String weekDay = weekPt[weekIndex];
        
        // Formata a data como "Qua, 22/04"
        String formattedDate = String.format(Locale.getDefault(), 
            "%s, %02d/%02d", 
            weekDay, 
            selectedDate.getDayOfMonth(), 
            selectedDate.getMonthValue()
        );
        
        tvSelectedDate.setText(formattedDate);
    }

    /**
     * Gera uma chave única para armazenar horários por data
     * Formato: "YYYY-MM-DD"
     */
    private String getDateKey(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * ViewContainer para cada célula de dia no calendário
     * Contém a referência ao TextView que exibe o número do dia
     */
    private static class DayViewContainer extends ViewContainer {
        final TextView textView;
        CalendarDay day;

        DayViewContainer(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
        }
    }
}
