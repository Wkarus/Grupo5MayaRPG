import { Module } from '@nestjs/common';

import { PacientesController }
from './pacientes.controller';

import { PacientesService }
from './pacientes.service';

import { PrismaModule }
from '../prisma/prisma.module';

import { AuthModule }
from '../auth/auth.module';

@Module({

  imports: [
    PrismaModule,
    AuthModule
  ],

  controllers: [PacientesController],

  providers: [PacientesService],

})

export class PacientesModule {}