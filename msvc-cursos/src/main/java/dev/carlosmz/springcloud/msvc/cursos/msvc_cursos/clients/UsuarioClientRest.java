package dev.carlosmz.springcloud.msvc.cursos.msvc_cursos.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import dev.carlosmz.springcloud.msvc.cursos.msvc_cursos.models.Usuario;

@FeignClient(name = "msvc-usuarios")
public interface UsuarioClientRest {

    @GetMapping("/{id}")
    Usuario detalle(@PathVariable Long id, @RequestHeader(value = "Authorization", required = true) String token);

    @PostMapping("/")
    Usuario crear(@RequestBody Usuario usuario, @RequestHeader(value = "Authorization", required = true) String token);

    @GetMapping("/usuarios-por-curso")
    List<Usuario> obtenerAlumnosPorCurso(@RequestParam Iterable<Long> ids,
            @RequestHeader(value = "Authorization", required = true) String token);

}
