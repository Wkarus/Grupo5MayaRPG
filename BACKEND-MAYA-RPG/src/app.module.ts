import { Module } from '@nestjs/common';

import { AgendamentosModule } from './agendamentos/agendamentos.module';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { AuthModule } from './auth/auth.module';
import { EvolucoesModule } from './evolucoes/evolucoes.module';
import { ExerciciosModule } from './exercicios/exercicios.module';
import { NotificacoesModule } from './notificacoes/notificacoes.module';
import { PacientesModule } from './pacientes/pacientes.module';
import { PrismaModule } from './prisma/prisma.module';
import { RelatoriosModule } from './relatorios/relatorios.module';
import { SessoesModule } from './sessoes/sessoes.module';
import { UsuariosModule } from './usuarios/usuarios.module';

@Module({
  imports: [
    PacientesModule,
    PrismaModule,
    AuthModule,
    UsuariosModule,
    ExerciciosModule,
    SessoesModule,
    EvolucoesModule,
    AgendamentosModule,
    RelatoriosModule,
    NotificacoesModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
