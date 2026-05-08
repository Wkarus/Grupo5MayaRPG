import {
  Body,
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Param,
  UseGuards

} from '@nestjs/common';

import { PacientesService } from './pacientes.service';


import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';

@UseGuards(JwtAuthGuard)
@Controller('pacientes')
export class PacientesController {

  constructor(
    private readonly pacientesService: PacientesService
  ) {}

  @Post()
  criar(@Body() dados: any) {

    return this.pacientesService.criar(dados);

  }

  @Get()
  listar() {

    return this.pacientesService.listar();

  }

  @Get(':id')
  buscar(@Param('id') id: string) {

    return this.pacientesService.buscarPorId(Number(id));

  }

  @Put(':id')
  atualizar(
  @Param('id') id: string,
  @Body() dados: any
  ){

  return this.pacientesService.atualizar(
    Number(id),
    dados
  );

}

@Delete(':id')
remover(@Param('id') id: string) {

  return this.pacientesService.remover(
    Number(id)
  );

}
}