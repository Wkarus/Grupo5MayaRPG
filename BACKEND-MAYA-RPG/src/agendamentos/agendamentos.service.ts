import { Injectable } from '@nestjs/common';

import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class AgendamentosService {
  constructor(private prisma: PrismaService) {}

  async criar(dados: any) {
    return this.prisma.agendamento.create({ data: dados });
  }

  async listar() {
    return this.prisma.agendamento.findMany({ include: { paciente: true } });
  }

  async listarPorPaciente(pacienteId: number) {
    return this.prisma.agendamento.findMany({ where: { pacienteId } });
  }

  async buscarPorId(id: number) {
    return this.prisma.agendamento.findUnique({
      where: { id },
      include: { paciente: true },
    });
  }

  async atualizar(id: number, dados: any) {
    return this.prisma.agendamento.update({
      where: { id },
      data: dados,
    });
  }

  async remover(id: number) {
    return this.prisma.agendamento.delete({ where: { id } });
  }
}
