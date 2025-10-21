package dev.carlosmz.springcloud.msvc.cursos.msvc_cursos.services;

import java.util.List;
import java.util.Optional;

import dev.carlosmz.springcloud.msvc.cursos.msvc_cursos.models.Usuario;
import dev.carlosmz.springcloud.msvc.cursos.msvc_cursos.models.entity.Curso;

public interface CursoService {

    List<Curso> listar();
    Optional<Curso> porId(Long id);
    Curso guardar(Curso curso);
    void eliminar(Long id);
    void eliminarCursoUsuarioPorId(Long id);
    Optional<Curso> porIdConUsuarios(Long id);

    Optional<Usuario> asignarUsuario(Usuario usuario, Long cursoId);
    Optional<Usuario> crearUsuario(Usuario usuario, Long cursoId);
    Optional<Usuario> eliminarUsuario(Usuario usuario, Long cursoId);

}
