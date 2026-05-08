import { Injectable } from '@nestjs/common';

import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class NotificacoesService {
  constructor(private prisma: PrismaService) {}

  async criar(dados: any) {
    return this.prisma.notificacao.create({ data: dados });
  }

  async listar() {
    return this.prisma.notificacao.findMany();
  }

  async listarNaoLidas() {
    return this.prisma.notificacao.findMany({
      where: { lida: false },
      orderBy: { data: 'desc' },
    });
  }

  async buscarPorId(id: number) {
    return this.prisma.notificacao.findUnique({ where: { id } });
  }

  async atualizar(id: number, dados: any) {
    return this.prisma.notificacao.update({
      where: { id },
      data: dados,
    });
  }

  async marcarComoLida(id: number) {
    return this.prisma.notificacao.update({
      where: { id },
      data: { lida: true },
    });
  }

  async remover(id: number) {
    return this.prisma.notificacao.delete({ where: { id } });
  }
}
