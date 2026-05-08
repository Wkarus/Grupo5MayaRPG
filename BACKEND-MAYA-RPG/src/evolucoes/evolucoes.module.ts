import { Module } from '@nestjs/common';

import { AuthModule } from '../auth/auth.module';
import { PrismaModule } from '../prisma/prisma.module';
import { EvolucoesController } from './evolucoes.controller';
import { EvolucoesService } from './evolucoes.service';

@Module({
  imports: [PrismaModule, AuthModule],
  controllers: [EvolucoesController],
  providers: [EvolucoesService],
})
export class EvolucoesModule {}
