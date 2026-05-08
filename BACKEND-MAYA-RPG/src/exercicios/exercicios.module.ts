import { Module } from '@nestjs/common';

import { AuthModule } from '../auth/auth.module';
import { PrismaModule } from '../prisma/prisma.module';
import { ExerciciosController } from './exercicios.controller';
import { ExerciciosService } from './exercicios.service';

@Module({
  imports: [PrismaModule, AuthModule],
  controllers: [ExerciciosController],
  providers: [ExerciciosService],
})
export class ExerciciosModule {}
