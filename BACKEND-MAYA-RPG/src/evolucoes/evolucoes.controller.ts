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
import { EvolucoesService } from './evolucoes.service';

@UseGuards(JwtAuthGuard)
@Controller('evolucoes')
export class EvolucoesController {
  constructor(private readonly evolucoesService: EvolucoesService) {}

  @Post()
  criar(@Body() dados: any) {
    return this.evolucoesService.criar(dados);
  }

  @Get()
  listar() {
    return this.evolucoesService.listar();
  }

  @Get('paciente/:pacienteId')
  listarPorPaciente(@Param('pacienteId') pacienteId: string) {
    return this.evolucoesService.listarPorPaciente(Number(pacienteId));
  }

  @Get(':id')
  buscar(@Param('id') id: string) {
    return this.evolucoesService.buscarPorId(Number(id));
  }

  @Put(':id')
  atualizar(@Param('id') id: string, @Body() dados: any) {
    return this.evolucoesService.atualizar(Number(id), dados);
  }

  @Delete(':id')
  remover(@Param('id') id: string) {
    return this.evolucoesService.remover(Number(id));
  }
}
