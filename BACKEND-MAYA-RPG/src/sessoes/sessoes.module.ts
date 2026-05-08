import { Module } from '@nestjs/common';

import { AuthModule } from '../auth/auth.module';
import { PrismaModule } from '../prisma/prisma.module';
import { SessoesController } from './sessoes.controller';
import { SessoesService } from './sessoes.service';

@Module({
  imports: [PrismaModule, AuthModule],
  controllers: [SessoesController],
  providers: [SessoesService],
})
export class SessoesModule {}
