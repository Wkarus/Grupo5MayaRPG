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
import { AgendamentosService } from './agendamentos.service';

@UseGuards(JwtAuthGuard)
@Controller('agendamentos')
export class AgendamentosController {
  constructor(private readonly agendamentosService: AgendamentosService) {}

  @Post()
  criar(@Body() dados: any) {
    return this.agendamentosService.criar(dados);
  }

  @Get()
  listar() {
    return this.agendamentosService.listar();
  }

  @Get('paciente/:pacienteId')
  listarPorPaciente(@Param('pacienteId') pacienteId: string) {
    return this.agendamentosService.listarPorPaciente(Number(pacienteId));
  }

  @Get(':id')
  buscar(@Param('id') id: string) {
    return this.agendamentosService.buscarPorId(Number(id));
  }

  @Put(':id')
  atualizar(@Param('id') id: string, @Body() dados: any) {
    return this.agendamentosService.atualizar(Number(id), dados);
  }

  @Delete(':id')
  remover(@Param('id') id: string) {
    return this.agendamentosService.remover(Number(id));
  }
}
