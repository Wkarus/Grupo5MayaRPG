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
import { SessoesService } from './sessoes.service';

@UseGuards(JwtAuthGuard)
@Controller('sessoes')
export class SessoesController {
  constructor(private readonly sessoesService: SessoesService) {}

  @Post()
  criar(@Body() dados: any) {
    return this.sessoesService.criar(dados);
  }

  @Get()
  listar() {
    return this.sessoesService.listar();
  }

  @Get('paciente/:pacienteId')
  listarPorPaciente(@Param('pacienteId') pacienteId: string) {
    return this.sessoesService.listarPorPaciente(Number(pacienteId));
  }

  @Get(':id')
  buscar(@Param('id') id: string) {
    return this.sessoesService.buscarPorId(Number(id));
  }

  @Put(':id')
  atualizar(@Param('id') id: string, @Body() dados: any) {
    return this.sessoesService.atualizar(Number(id), dados);
  }

  @Delete(':id')
  remover(@Param('id') id: string) {
    return this.sessoesService.remover(Number(id));
  }
}
