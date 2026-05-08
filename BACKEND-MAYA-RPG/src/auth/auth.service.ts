import {
  Injectable,
  UnauthorizedException
} from '@nestjs/common';

import { JwtService }
from '@nestjs/jwt';

import { UsuariosService }
from '../usuarios/usuarios.service';

import * as bcrypt from 'bcrypt';

@Injectable()
export class AuthService {

  constructor(

    private jwtService: JwtService,

    private usuariosService: UsuariosService,

  ) {}

  async login(email: string, senha: string) {

    const usuario =
      await this.usuariosService.buscarPorEmail(email);

    if (!usuario) {

      throw new UnauthorizedException(
        'Usuário não encontrado'
      );

    }

    const senhaCorreta =
      await bcrypt.compare(
        senha,
        usuario.senha
      );

    if (!senhaCorreta) {

      throw new UnauthorizedException(
        'Senha inválida'
      );

    }

    const payload = {

      sub: usuario.id,

      email: usuario.email,

      tipo: usuario.tipo,

    };

    const token = this.jwtService.sign(payload);

    return {
      token,
      user: {
        id: usuario.id,
        email: usuario.email,
        nome: usuario.nome,
        role: String(usuario.tipo).toUpperCase(),
      },
    };

  }

}