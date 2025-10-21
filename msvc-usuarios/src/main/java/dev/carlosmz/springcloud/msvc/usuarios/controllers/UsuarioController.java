package dev.carlosmz.springcloud.msvc.usuarios.controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.carlosmz.springcloud.msvc.usuarios.models.entity.Usuario;
import dev.carlosmz.springcloud.msvc.usuarios.services.UsuarioService;
import jakarta.validation.Valid;

@RestController
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Environment env;

    @GetMapping("/crash")
    public void getMethodName() {
        ((ConfigurableApplicationContext) context).close();
    }
    
    @GetMapping
    public ResponseEntity<?> listar() {
        Map<String, Object> body = new HashMap<>();
        body.put("users", usuarioService.listar());
        body.put("pod_info", env.getProperty("MY_POD_NAME") + ": " + env.getProperty("MY_POD_IP"));
        body.put("texto", env.getProperty("config.texto"));
        // return Collections.singletonMap("users", usuarioService.listar());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id) {
        Optional<Usuario> optUsuario = usuarioService.porId(id);
        if (optUsuario.isPresent()) {
            return ResponseEntity.ok().body(optUsuario.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Usuario usuario, BindingResult result) {
        if (result.hasErrors()) {
            return validar(result);
        }
        
        if (!usuario.getEmail().isEmpty() && usuarioService.existePorEmail(usuario.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(Collections
                            .singletonMap("mensaje", "Ya existe un usuario con ese correo electrónico."));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(@Valid @RequestBody Usuario usuario, BindingResult result, @PathVariable Long id) {
        if (result.hasErrors()) {
            return validar(result);
        }

        Optional<Usuario> optUsuario = usuarioService.porId(id);
        if (optUsuario.isPresent()) {
            Usuario usuarioEncontrado = optUsuario.get();
            if (!usuario.getEmail().isEmpty()
                    && !usuario.getEmail().equalsIgnoreCase(usuarioEncontrado.getEmail())
                    && usuarioService.porEmail(usuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Collections
                                .singletonMap("mensaje", "Ya existe un usuario con ese correo electrónico."));
            }
            usuarioEncontrado.setNombre(usuario.getNombre());
            usuarioEncontrado.setEmail(usuario.getEmail());
            usuarioEncontrado.setPassword(usuario.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(usuarioEncontrado));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        Optional<Usuario> optUsuario = usuarioService.porId(id);
        if (optUsuario.isPresent()) {
            usuarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/usuarios-por-curso")
    public ResponseEntity<?> obtenerAlumnosPorCurso(@RequestParam List<Long> ids) {
        return ResponseEntity.ok().body(usuarioService.listarPorIds(ids));
    }

    private ResponseEntity<?> validar(BindingResult result) {
        Map<String, String> errores = new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errores.put(err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errores);
    }

}
