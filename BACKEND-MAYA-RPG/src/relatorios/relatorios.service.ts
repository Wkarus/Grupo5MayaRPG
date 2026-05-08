import { Injectable } from '@nestjs/common';

import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class RelatoriosService {
  constructor(private prisma: PrismaService) {}

  async criar(dados: any) {
    return this.prisma.relatorio.create({ data: dados });
  }

  async listar() {
    return this.prisma.relatorio.findMany();
  }

  async buscarPorId(id: number) {
    return this.prisma.relatorio.findUnique({ where: { id } });
  }

  async atualizar(id: number, dados: any) {
    return this.prisma.relatorio.update({
      where: { id },
      data: dados,
    });
  }

  async remover(id: number) {
    return this.prisma.relatorio.delete({ where: { id } });
  }
}
