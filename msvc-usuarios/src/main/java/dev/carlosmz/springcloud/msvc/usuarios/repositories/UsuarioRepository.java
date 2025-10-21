package dev.carlosmz.springcloud.msvc.usuarios.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import dev.carlosmz.springcloud.msvc.usuarios.models.entity.Usuario;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

}
