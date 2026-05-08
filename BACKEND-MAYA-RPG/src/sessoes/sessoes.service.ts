import { Injectable } from '@nestjs/common';

import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class SessoesService {
  constructor(private prisma: PrismaService) {}

  async criar(dados: any) {
    return this.prisma.sessao.create({ data: dados });
  }

  async listar() {
    return this.prisma.sessao.findMany({ include: { paciente: true } });
  }

  async listarPorPaciente(pacienteId: number) {
    return this.prisma.sessao.findMany({ where: { pacienteId } });
  }

  async buscarPorId(id: number) {
    return this.prisma.sessao.findUnique({
      where: { id },
      include: { paciente: true },
    });
  }

  async atualizar(id: number, dados: any) {
    return this.prisma.sessao.update({
      where: { id },
      data: dados,
    });
  }

  async remover(id: number) {
    return this.prisma.sessao.delete({ where: { id } });
  }
}
