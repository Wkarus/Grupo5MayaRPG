import { Injectable } from '@nestjs/common';

import { PrismaService }
from '../prisma/prisma.service';

import * as bcrypt from 'bcrypt';

@Injectable()
export class UsuariosService {

  constructor(
    private prisma: PrismaService
  ) {}

  async criar(dados: any) {

    const senhaHash =
      await bcrypt.hash(dados.senha, 10);

    return this.prisma.usuario.create({

      data: {

        nome: dados.nome,

        email: dados.email,

        senha: senhaHash,

        celular: dados.celular,

        tipo: dados.tipo,

      },

    });

  }

  async buscarPorEmail(email: string) {

    return this.prisma.usuario.findUnique({

      where: { email },

    });

  }

}