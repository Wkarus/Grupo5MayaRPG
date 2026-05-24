package com.example.mayarpg;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mayarpg.network.ApiClient;
import com.example.mayarpg.network.ApiConfig;
import com.example.mayarpg.network.SessionManager;
import com.example.mayarpg.network.services.CommentService;
import com.example.mayarpg.network.services.PostsService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String KEY_NOME = "key_nome";
    private static final String PREFS_HOME = "mayarpg_home";
    private static final String PREF_KEY_REPLY_PREFIX = "maya_reply_";

    public static HomeFragment newInstance(String nomeUsuario) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(KEY_NOME, nomeUsuario == null ? "Paciente" : nomeUsuario);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvBoasVindas = view.findViewById(R.id.tvBoasVindas);
        ImageButton btnLogoutTop = view.findViewById(R.id.btnLogoutTop);

        String nome = resolveNomeCliente();

        tvBoasVindas.setText(getString(R.string.bem_vindo, nome));

        TextView tvMensagemMaya = view.findViewById(R.id.tvMensagemMaya);
        tvMensagemMaya.setText(getString(R.string.maya_mensagem_cliente, nome));

        btnLogoutTop.setOnClickListener(v -> showLogoutDialog());

        // Próxima sessão pinada: clique abre aba Agenda
        view.findViewById(R.id.cardProximaSessao).setOnClickListener(v -> navigateToAgenda());

        setupMayaReply(view);
        loadMayaPosts(view);
        SessionBookingFirestore.fetchAndCacheLocal(requireContext(), () -> {
            if (isAdded() && getView() != null) {
                refreshProximaSessaoUi(getView());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        if (v != null) {
            loadMayaPosts(v);
            SessionBookingFirestore.fetchAndCacheLocal(requireContext(), () -> {
                if (isAdded() && getView() != null) {
                    refreshProximaSessaoUi(getView());
                }
            });
        }
    }

    private void refreshProximaSessaoUi(View root) {
        TextView tv = root.findViewById(R.id.tvProximaSessaoResumo);
        if (tv == null || getContext() == null) {
            return;
        }
        if (SessionBookingPreferences.hasBooking(requireContext())) {
            String summary = SessionBookingPreferences.formatHomeSummary(requireContext());
            tv.setText(summary != null ? summary : getString(R.string.proxima_sessao_agendar_hint));
            tv.setBackgroundResource(R.drawable.bg_button_green);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_primary));
        } else {
            tv.setText(R.string.proxima_sessao_agendar_hint);
            tv.setBackgroundResource(R.drawable.bg_chip_blue);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_on_primary));
        }
    }

    /**
     * Nome mostrado na home: argumentos da Activity (login) ou Firebase Auth (displayName / email).
     */
    private String resolveNomeCliente() {
        String nome = "Paciente";
        if (getArguments() != null) {
            nome = getArguments().getString(KEY_NOME, "Paciente");
        }
        if (!"Paciente".equals(nome) && nome != null && !nome.trim().isEmpty()) {
            return nome.trim();
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String display = user.getDisplayName();
            if (display != null && !display.trim().isEmpty()) {
                return display.trim();
            }
            String email = user.getEmail();
            if (email != null && !email.isEmpty()) {
                int at = email.indexOf('@');
                String local = at > 0 ? email.substring(0, at) : email;
                if (!local.isEmpty()) {
                    return local;
                }
            }
        }
        return nome != null && !nome.trim().isEmpty() ? nome.trim() : "Paciente";
    }

    private void setupMayaReply(View root) {
        TextView btnResponder = root.findViewById(R.id.btnResponderMaya);
        LinearLayout layoutComposer = root.findViewById(R.id.layoutReplyComposer);
        EditText etReply = root.findViewById(R.id.etReplyMaya);
        TextView btnEnviar = root.findViewById(R.id.btnEnviarResposta);
        LinearLayout layoutSaved = root.findViewById(R.id.layoutSavedReply);
        TextView tvSaved = root.findViewById(R.id.tvSavedReplyMaya);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_HOME, android.content.Context.MODE_PRIVATE);
        String replyKey = replyPreferenceKey();

        Runnable refreshSaved = () -> {
            String saved = prefs.getString(replyKey, "").trim();
            if (saved.isEmpty()) {
                layoutSaved.setVisibility(View.GONE);
                tvSaved.setText("");
            } else {
                layoutSaved.setVisibility(View.VISIBLE);
                tvSaved.setText(saved);
            }
        };

        refreshSaved.run();

        btnResponder.setOnClickListener(v -> {
            boolean opening = layoutComposer.getVisibility() != View.VISIBLE;
            layoutComposer.setVisibility(opening ? View.VISIBLE : View.GONE);
            if (opening) {
                String draft = prefs.getString(replyKey, "");
                etReply.setText(draft);
                etReply.requestFocus();
                etReply.setSelection(etReply.length());
            }
        });

        btnEnviar.setOnClickListener(v -> {
            String text = etReply.getText() != null ? etReply.getText().toString().trim() : "";
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(requireContext(), R.string.resposta_vazia_toast, Toast.LENGTH_SHORT).show();
                return;
            }
            btnEnviar.setEnabled(false);
            ApiClient.commentService(requireContext())
                    .create(new CommentService.CreateBody(text))
                    .enqueue(new Callback<CommentService.CreateResponse>() {
                        @Override
                        public void onResponse(
                                @NonNull Call<CommentService.CreateResponse> call,
                                @NonNull Response<CommentService.CreateResponse> response
                        ) {
                            btnEnviar.setEnabled(true);
                            if (!isAdded()) {
                                return;
                            }
                            if (!response.isSuccessful()) {
                                Toast.makeText(requireContext(), R.string.resposta_erro_toast, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            prefs.edit().putString(replyKey, text).apply();
                            etReply.setText("");
                            layoutComposer.setVisibility(View.GONE);
                            refreshSaved.run();
                            Toast.makeText(requireContext(), R.string.resposta_salva_toast, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(@NonNull Call<CommentService.CreateResponse> call, @NonNull Throwable t) {
                            btnEnviar.setEnabled(true);
                            if (isAdded()) {
                                Toast.makeText(requireContext(), R.string.resposta_erro_toast, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }

    private void loadMayaPosts(View root) {
        LinearLayout container = root.findViewById(R.id.postsFeedContainer);
        if (container == null || getContext() == null) {
            return;
        }

        ApiClient.postsService(requireContext()).getPosts().enqueue(new Callback<List<PostsService.PostDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<PostsService.PostDto>> call,
                                   @NonNull Response<List<PostsService.PostDto>> response) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                container.removeAllViews();
                List<PostsService.PostDto> posts = response.body();
                if (!response.isSuccessful() || posts == null || posts.isEmpty()) {
                    TextView empty = new TextView(requireContext());
                    empty.setText(R.string.sem_publicacoes_maya);
                    empty.setTextColor(0xFF888888);
                    empty.setPadding(0, 8, 0, 16);
                    container.addView(empty);
                    return;
                }
                LayoutInflater inflater = LayoutInflater.from(requireContext());
                for (PostsService.PostDto post : posts) {
                    View item = inflater.inflate(R.layout.item_maya_post, container, false);
                    bindPostItem(item, post);
                    container.addView(item);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PostsService.PostDto>> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                container.removeAllViews();
                TextView empty = new TextView(requireContext());
                empty.setText(R.string.sem_publicacoes_maya);
                empty.setTextColor(0xFF888888);
                container.addView(empty);
            }
        });
    }

    private void bindPostItem(View item, PostsService.PostDto post) {
        TextView tvTitulo = item.findViewById(R.id.tvPostTitulo);
        TextView tvConteudo = item.findViewById(R.id.tvPostConteudo);
        ImageView img = item.findViewById(R.id.imgPostMedia);
        LinearLayout layoutVerVideo = item.findViewById(R.id.layoutVerVideo);
        TextView btnVideo = item.findViewById(R.id.btnVerVideo);
        LinearLayout btnComentar = item.findViewById(R.id.btnComentarPost);
        TextView tvCommentCount = item.findViewById(R.id.tvCommentCount);
        LinearLayout btnLike = item.findViewById(R.id.btnLikePost);
        TextView tvLikeHeart = item.findViewById(R.id.tvLikeHeart);
        TextView tvLikeCount = item.findViewById(R.id.tvLikeCount);
        LinearLayout layoutCommentsList = item.findViewById(R.id.layoutCommentsList);
        LinearLayout layoutComposer = item.findViewById(R.id.layoutPostComposer);
        EditText etComment = item.findViewById(R.id.etPostComment);
        TextView btnEnviar = item.findViewById(R.id.btnEnviarPostComment);

        // Título
        if (!TextUtils.isEmpty(post.titulo)) {
            tvTitulo.setVisibility(View.VISIBLE);
            tvTitulo.setText(post.titulo);
        }
        // Conteúdo
        if (!TextUtils.isEmpty(post.conteudo)) {
            tvConteudo.setVisibility(View.VISIBLE);
            tvConteudo.setText(post.conteudo);
        }

        // Mídia
        String mediaUrl = resolveMediaUrl(post.mediaUrl);
        if ("VIDEO".equalsIgnoreCase(post.tipo) && !TextUtils.isEmpty(mediaUrl)) {
            layoutVerVideo.setVisibility(View.VISIBLE);
            String finalUrl = mediaUrl;
            layoutVerVideo.setOnClickListener(v ->
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))));
        } else if ("IMAGEM".equalsIgnoreCase(post.tipo) && !TextUtils.isEmpty(mediaUrl)) {
            img.setVisibility(View.VISIBLE);
            loadImageAsync(img, mediaUrl);
            String finalUrl = mediaUrl;
            img.setOnClickListener(v ->
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))));
        }

        // Likes (estado local em SharedPreferences)
        int postIntId = (int) post.id;
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_HOME, android.content.Context.MODE_PRIVATE);
        String likeKey = "like_post_" + postIntId;
        boolean[] liked = {prefs.getBoolean(likeKey, false)};
        int[] likeCount = {prefs.getInt(likeKey + "_count", 0)};
        updateLikeUi(tvLikeHeart, tvLikeCount, liked[0], likeCount[0]);
        btnLike.setOnClickListener(v -> {
            liked[0] = !liked[0];
            likeCount[0] = liked[0] ? likeCount[0] + 1 : Math.max(0, likeCount[0] - 1);
            prefs.edit()
                    .putBoolean(likeKey, liked[0])
                    .putInt(likeKey + "_count", likeCount[0])
                    .apply();
            updateLikeUi(tvLikeHeart, tvLikeCount, liked[0], likeCount[0]);
        });

        // Carregar comentários do post
        loadPostComments(postIntId, layoutCommentsList, tvCommentCount);

        // Clique em 💬 expande/colapsa comentários + composer
        btnComentar.setOnClickListener(v -> {
            boolean open = layoutCommentsList.getVisibility() == View.VISIBLE;
            layoutCommentsList.setVisibility(open ? View.GONE : View.VISIBLE);
            layoutComposer.setVisibility(open ? View.GONE : View.VISIBLE);
            if (!open) {
                etComment.requestFocus();
            }
        });

        // Enviar comentário
            btnEnviar.setOnClickListener(v -> {
            String text = etComment.getText() != null ? etComment.getText().toString().trim() : "";
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(requireContext(), R.string.resposta_vazia_toast, Toast.LENGTH_SHORT).show();
                return;
            }
            btnEnviar.setEnabled(false);
            ApiClient.commentService(requireContext())
                    .create(new CommentService.CreateBody(text, postIntId))
                    .enqueue(new Callback<CommentService.CreateResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<CommentService.CreateResponse> call2,
                                               @NonNull Response<CommentService.CreateResponse> resp) {
                            if (!isAdded()) return;
                            btnEnviar.setEnabled(true);
                            if (resp.isSuccessful()) {
                                etComment.setText("");
                                layoutComposer.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), R.string.resposta_salva_toast, Toast.LENGTH_SHORT).show();
                                // Recarrega comentários para mostrar o novo
                                loadPostComments(postIntId, layoutCommentsList, tvCommentCount);
                            } else {
                                Toast.makeText(requireContext(), R.string.resposta_erro_toast, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<CommentService.CreateResponse> call2, @NonNull Throwable t) {
                            if (!isAdded()) return;
                            btnEnviar.setEnabled(true);
                            Toast.makeText(requireContext(), R.string.resposta_erro_toast, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void loadPostComments(int postId, LinearLayout listContainer, TextView tvCount) {
        if (!isAdded() || getContext() == null) return;
        ApiClient.commentService(requireContext())
                .getPostComments(postId)
                .enqueue(new Callback<List<CommentService.PostComment>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CommentService.PostComment>> call,
                                           @NonNull Response<List<CommentService.PostComment>> response) {
                        if (!isAdded() || getContext() == null) return;
                        listContainer.removeAllViews();
                        List<CommentService.PostComment> comments = response.body();
                        if (comments == null || comments.isEmpty()) {
                            tvCount.setText("0");
                            return;
                        }
                        tvCount.setText(String.valueOf(comments.size()));
                        LayoutInflater inflater = LayoutInflater.from(requireContext());
                        for (CommentService.PostComment c : comments) {
                            View bubble = inflater.inflate(R.layout.item_post_comment, listContainer, false);
                            TextView tvText = bubble.findViewById(R.id.tvCommentText);
                            LinearLayout layoutMayaReply = bubble.findViewById(R.id.layoutMayaReply);
                            TextView tvMayaReply = bubble.findViewById(R.id.tvMayaReplyText);
                            tvText.setText(c.texto);
                            if (!TextUtils.isEmpty(c.resposta)) {
                                layoutMayaReply.setVisibility(View.VISIBLE);
                                tvMayaReply.setText(c.resposta);
                            }
                            listContainer.addView(bubble);
                        }
                        listContainer.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<CommentService.PostComment>> call, @NonNull Throwable t) {
                        // Sem conexão: mantém lista oculta
                    }
                });
    }

    private void updateLikeUi(TextView heart, TextView count, boolean liked, int total) {
        heart.setText(liked ? "❤️" : "🤍");
        count.setText(total > 0 ? String.valueOf(total) : "");
        count.setTextColor(liked ? 0xFFE0245E : 0xFF555555);
    }

    private void navigateToAgenda() {
        if (!isAdded() || getActivity() == null) return;
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_schedule);
        }
    }

    private String resolveMediaUrl(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        String base = ApiConfig.resolveBaseUrl(requireContext());
        if (base.endsWith("/") && path.startsWith("/")) {
            return base + path.substring(1);
        }
        if (!base.endsWith("/") && !path.startsWith("/")) {
            return base + "/" + path;
        }
        return base + path;
    }

    private void loadImageAsync(ImageView target, String url) {
        new Thread(() -> {
            try (InputStream in = new URL(url).openStream()) {
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded() && bitmap != null) {
                        target.setImageBitmap(bitmap);
                    }
                });
            } catch (Exception ignored) {
                // Mantem card sem imagem se falhar download.
            }
        }).start();
    }

    private String replyPreferenceKey() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid() != null && !user.getUid().isEmpty()) {
            return PREF_KEY_REPLY_PREFIX + user.getUid();
        }
        return PREF_KEY_REPLY_PREFIX + "guest";
    }

    // Dialog de confirmacao para evitar logout acidental.
    private void showLogoutDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_logout, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        Button btnCancelar = dialogView.findViewById(R.id.btnCancelarLogout);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmarLogout);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnConfirmar.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            // Limpa sessao JWT local para evitar token antigo apos logout.
            new SessionManager(requireContext()).clear();

            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            dialog.dismiss();
        });

        dialog.show();
    }
}
