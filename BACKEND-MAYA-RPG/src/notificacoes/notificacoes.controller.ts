import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Patch,
  Post,
  Put,
  UseGuards,
} from '@nestjs/common';

import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { NotificacoesService } from './notificacoes.service';

@UseGuards(JwtAuthGuard)
@Controller('notificacoes')
export class NotificacoesController {
  constructor(private readonly notificacoesService: NotificacoesService) {}

  @Post()
  criar(@Body() dados: any) {
    return this.notificacoesService.criar(dados);
  }

  @Get()
  listar() {
    return this.notificacoesService.listar();
  }

  @Get('pendentes/lista')
  listarNaoLidas() {
    return this.notificacoesService.listarNaoLidas();
  }

  @Get(':id')
  buscar(@Param('id') id: string) {
    return this.notificacoesService.buscarPorId(Number(id));
  }

  @Patch(':id/lida')
  marcarLida(@Param('id') id: string) {
    return this.notificacoesService.marcarComoLida(Number(id));
  }

  @Put(':id')
  atualizar(@Param('id') id: string, @Body() dados: any) {
    return this.notificacoesService.atualizar(Number(id), dados);
  }

  @Delete(':id')
  remover(@Param('id') id: string) {
    return this.notificacoesService.remover(Number(id));
  }
}
