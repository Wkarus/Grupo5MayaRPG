import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Post,
  Put,
  UseGuards,
} from '@nestjs/common';

import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { ExerciciosService } from './exercicios.service';

@UseGuards(JwtAuthGuard)
@Controller('exercicios')
export class ExerciciosController {
  constructor(private readonly exerciciosService: ExerciciosService) {}

  @Post()
  criar(@Body() dados: any) {
    return this.exerciciosService.criar(dados);
  }

  @Get()
  listar() {
    return this.exerciciosService.listar();
  }

  @Get('paciente/:pacienteId')
  listarPorPaciente(@Param('pacienteId') pacienteId: string) {
    return this.exerciciosService.listarPorPaciente(Number(pacienteId));
  }

  @Get(':id')
  buscar(@Param('id') id: string) {
    return this.exerciciosService.buscarPorId(Number(id));
  }

  @Put(':id')
  atualizar(@Param('id') id: string, @Body() dados: any) {
    return this.exerciciosService.atualizar(Number(id), dados);
  }

  @Delete(':id')
  remover(@Param('id') id: string) {
    return this.exerciciosService.remover(Number(id));
  }
}
