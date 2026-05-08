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
import { RelatoriosService } from './relatorios.service';

@UseGuards(JwtAuthGuard)
@Controller('relatorios')
export class RelatoriosController {
  constructor(private readonly relatoriosService: RelatoriosService) {}

  @Post()
  criar(@Body() dados: any) {
    return this.relatoriosService.criar(dados);
  }

  @Get()
  listar() {
    return this.relatoriosService.listar();
  }

  @Get(':id')
  buscar(@Param('id') id: string) {
    return this.relatoriosService.buscarPorId(Number(id));
  }

  @Put(':id')
  atualizar(@Param('id') id: string, @Body() dados: any) {
    return this.relatoriosService.atualizar(Number(id), dados);
  }

  @Delete(':id')
  remover(@Param('id') id: string) {
    return this.relatoriosService.remover(Number(id));
  }
}
