package com.ips.gestion_academica.service;

import com.ips.gestion_academica.dto.usuario.UsuarioRequest;
import com.ips.gestion_academica.dto.usuario.UsuarioResponse;
import com.ips.gestion_academica.exception.RecursoDuplicadoException;
import com.ips.gestion_academica.exception.RecursoNoEncontradoException;
import com.ips.gestion_academica.exception.RecursoInactivoException;
import com.ips.gestion_academica.model.Rol;
import com.ips.gestion_academica.model.Usuario;
import com.ips.gestion_academica.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioRequest crearRequestValido() {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Sofia");
        request.setApellido("Danieli");
        request.setDni("40111222");
        request.setEmail("sofia@mail.com");
        request.setPassword("1234");
        request.setLegajo("A123");
        request.setRol(Rol.ALUMNO);

        return request;
    }

    private Usuario crearUsuarioActivo() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Sofia");
        usuario.setApellido("Danieli");
        usuario.setDni("40111222");
        usuario.setEmail("sofia@mail.com");
        usuario.setPassword("1234");
        usuario.setLegajo("A123");
        usuario.setRol(Rol.ALUMNO);
        usuario.setActivo(true);

        return usuario;
    }

    @Test
    void crearUsuario_deberiaCrearUsuarioCorrectamente() {
        UsuarioRequest request = crearRequestValido();

        when(usuarioRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(usuarioRepository.existsByDni(request.getDni()))
                .thenReturn(false);

        when(usuarioRepository.existsByLegajo(request.getLegajo()))
                .thenReturn(false);

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario usuario = invocation.getArgument(0);
                    usuario.setId(1L);
                    return usuario;
                });

        UsuarioResponse response = usuarioService.crearUsuario(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Sofia", response.getNombre());
        assertEquals("Danieli", response.getApellido());
        assertEquals("sofia@mail.com", response.getEmail());
        assertEquals("A123", response.getLegajo());

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(captor.capture());

        Usuario usuarioGuardado = captor.getValue();

        assertEquals("40111222", usuarioGuardado.getDni());
        assertEquals("1234", usuarioGuardado.getPassword());
        assertEquals(Rol.ALUMNO, usuarioGuardado.getRol());
        assertTrue(usuarioGuardado.getActivo());
    }

    @Test
    void crearUsuario_deberiaLanzarErrorCuandoEmailEstaDuplicado() {
        UsuarioRequest request = crearRequestValido();

        when(usuarioRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioService.crearUsuario(request)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));

        // Como falla en la primera validación, no debería seguir consultando.
        verify(usuarioRepository, never()).existsByDni(anyString());
        verify(usuarioRepository, never()).existsByLegajo(anyString());
    }

    @Test
    void crearUsuario_deberiaLanzarErrorCuandoDniEstaDuplicado() {
        UsuarioRequest request = crearRequestValido();

        when(usuarioRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(usuarioRepository.existsByDni(request.getDni()))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioService.crearUsuario(request)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(usuarioRepository, never()).existsByLegajo(anyString());
    }

    @Test
    void crearUsuario_deberiaLanzarErrorCuandoLegajoEstaDuplicado() {
        UsuarioRequest request = crearRequestValido();

        when(usuarioRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(usuarioRepository.existsByDni(request.getDni()))
                .thenReturn(false);

        when(usuarioRepository.existsByLegajo(request.getLegajo()))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioService.crearUsuario(request)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void listarUsuarios_deberiaDevolverSoloUsuariosActivos() {
        Usuario usuario = crearUsuarioActivo();

        when(usuarioRepository.findByActivoTrue())
                .thenReturn(List.of(usuario));

        List<UsuarioResponse> resultado =
                usuarioService.listarUsuarios();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("Sofia", resultado.get(0).getNombre());

        verify(usuarioRepository).findByActivoTrue();
    }

    @Test
    void buscarPorId_deberiaDevolverUsuarioActivo() {
        Usuario usuario = crearUsuarioActivo();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        UsuarioResponse response =
                usuarioService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Sofia", response.getNombre());
        assertEquals("A123", response.getLegajo());
    }

    @Test
    void buscarPorId_deberiaLanzarErrorCuandoUsuarioNoExiste() {
        when(usuarioRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> usuarioService.buscarPorId(999L)
        );
    }

    @Test
    void buscarPorId_deberiaLanzarErrorCuandoUsuarioEstaInactivo() {
        Usuario usuario = crearUsuarioActivo();
        usuario.setActivo(false);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                RecursoInactivoException.class,
                () -> usuarioService.buscarPorId(1L)
        );
    }

    @Test
    void modificarUsuario_deberiaModificarUsuarioCorrectamente() {
        Usuario usuarioExistente = crearUsuarioActivo();

        UsuarioRequest request = crearRequestValido();
        request.setNombre("Sofi");
        request.setApellido("Danieli modificada");
        request.setEmail("sofia.nueva@mail.com");
        request.setDni("40111223");
        request.setLegajo("A124");
        request.setRol(Rol.ADMIN);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioExistente));

        when(usuarioRepository.existsByEmailAndIdNot(
                request.getEmail(), 1L))
                .thenReturn(false);

        when(usuarioRepository.existsByDniAndIdNot(
                request.getDni(), 1L))
                .thenReturn(false);

        when(usuarioRepository.existsByLegajoAndIdNot(
                request.getLegajo(), 1L))
                .thenReturn(false);

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResponse response =
                usuarioService.modificarUsuario(1L, request);

        assertNotNull(response);
        assertEquals("Sofi", response.getNombre());
        assertEquals("Danieli modificada", response.getApellido());
        assertEquals("sofia.nueva@mail.com", response.getEmail());
        assertEquals("A124", response.getLegajo());

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).save(captor.capture());

        Usuario usuarioModificado = captor.getValue();

        assertEquals("40111223", usuarioModificado.getDni());
        assertEquals(Rol.ADMIN, usuarioModificado.getRol());
        assertTrue(usuarioModificado.getActivo());
    }

    @Test
    void modificarUsuario_deberiaLanzarErrorCuandoUsuarioNoExiste() {
        UsuarioRequest request = crearRequestValido();

        when(usuarioRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> usuarioService.modificarUsuario(999L, request)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void modificarUsuario_deberiaLanzarErrorCuandoEmailPerteneceAOtroUsuario() {
        Usuario usuarioExistente = crearUsuarioActivo();
        UsuarioRequest request = crearRequestValido();

        request.setEmail("otro@mail.com");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioExistente));

        when(usuarioRepository.existsByEmailAndIdNot(
                request.getEmail(), 1L))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioService.modificarUsuario(1L, request)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void modificarUsuario_deberiaLanzarErrorCuandoDniPerteneceAOtroUsuario() {
        Usuario usuarioExistente = crearUsuarioActivo();
        UsuarioRequest request = crearRequestValido();

        request.setDni("99999999");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioExistente));

        when(usuarioRepository.existsByEmailAndIdNot(
                request.getEmail(), 1L))
                .thenReturn(false);

        when(usuarioRepository.existsByDniAndIdNot(
                request.getDni(), 1L))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioService.modificarUsuario(1L, request)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void modificarUsuario_deberiaLanzarErrorCuandoLegajoPerteneceAOtroUsuario() {
        Usuario usuarioExistente = crearUsuarioActivo();
        UsuarioRequest request = crearRequestValido();

        request.setLegajo("A999");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioExistente));

        when(usuarioRepository.existsByEmailAndIdNot(
                request.getEmail(), 1L))
                .thenReturn(false);

        when(usuarioRepository.existsByDniAndIdNot(
                request.getDni(), 1L))
                .thenReturn(false);

        when(usuarioRepository.existsByLegajoAndIdNot(
                request.getLegajo(), 1L))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> usuarioService.modificarUsuario(1L, request)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void darDeBaja_deberiaMarcarUsuarioComoInactivo() {
        Usuario usuario = crearUsuarioActivo();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.darDeBaja(1L);

        assertFalse(usuario.getActivo());

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void darDeBaja_deberiaLanzarErrorCuandoUsuarioNoExiste() {
        when(usuarioRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> usuarioService.darDeBaja(999L)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void darDeBaja_deberiaLanzarErrorCuandoUsuarioYaEstaInactivo() {
        Usuario usuario = crearUsuarioActivo();
        usuario.setActivo(false);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                RecursoInactivoException.class,
                () -> usuarioService.darDeBaja(1L)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}