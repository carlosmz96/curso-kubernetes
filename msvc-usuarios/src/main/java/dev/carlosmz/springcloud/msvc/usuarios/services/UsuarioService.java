package dev.carlosmz.springcloud.msvc.usuarios.services;

import java.util.List;
import java.util.Optional;

import dev.carlosmz.springcloud.msvc.usuarios.models.entity.Usuario;

public interface UsuarioService {

    List<Usuario> listar();
    Optional<Usuario> porId(Long id);
    Usuario guardar(Usuario usuario);
    void eliminar(Long id);
    List<Usuario> listarPorIds(Iterable<Long> ids);

    Optional<Usuario> porEmail(String email);
    boolean existePorEmail(String email);

}
