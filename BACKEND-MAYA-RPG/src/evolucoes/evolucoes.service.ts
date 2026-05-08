import { Injectable } from '@nestjs/common';

import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class EvolucoesService {
  constructor(private prisma: PrismaService) {}

  async criar(dados: any) {
    return this.prisma.evolucao.create({ data: dados });
  }

  async listar() {
    return this.prisma.evolucao.findMany({ include: { paciente: true } });
  }

  async listarPorPaciente(pacienteId: number) {
    return this.prisma.evolucao.findMany({ where: { pacienteId } });
  }

  async buscarPorId(id: number) {
    return this.prisma.evolucao.findUnique({
      where: { id },
      include: { paciente: true },
    });
  }

  async atualizar(id: number, dados: any) {
    return this.prisma.evolucao.update({
      where: { id },
      data: dados,
    });
  }

  async remover(id: number) {
    return this.prisma.evolucao.delete({ where: { id } });
  }
}
