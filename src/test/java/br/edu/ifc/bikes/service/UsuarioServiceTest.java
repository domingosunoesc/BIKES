package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.dto.UsuarioSenhaDTO;
import br.edu.ifc.bikes.dto.mapper.UsuarioMapper;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.PasswordInvalidException;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final Long USER_ID = 1L;
    private static final String CURRENT_PASSWORD = "senha1";
    private static final String NEW_PASSWORD = "senha2";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(USER_ID);
        usuario.setUsername("usuario");
        usuario.setPassword(CURRENT_PASSWORD);
    }

    @Test
    void updatePasswordUpdatesPasswordWhenCurrentAndConfirmationAreValid() {
        UsuarioSenhaDTO senhaDTO = new UsuarioSenhaDTO(NEW_PASSWORD, NEW_PASSWORD, CURRENT_PASSWORD);
        UsuarioResponseDTO response = new UsuarioResponseDTO(USER_ID, "usuario", "CLIENTE");
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);

        UsuarioResponseDTO result = usuarioService.updatePassword(USER_ID, senhaDTO);

        assertEquals(NEW_PASSWORD, usuario.getPassword());
        assertSame(response, result);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void updatePasswordRejectsIncorrectCurrentPassword() {
        UsuarioSenhaDTO senhaDTO = new UsuarioSenhaDTO(NEW_PASSWORD, NEW_PASSWORD, "errada1");
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuario));

        PasswordInvalidException exception = assertThrows(
                PasswordInvalidException.class,
                () -> usuarioService.updatePassword(USER_ID, senhaDTO)
        );

        assertEquals("A senha atual não confere", exception.getMessage());
        assertEquals(CURRENT_PASSWORD, usuario.getPassword());
        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    void updatePasswordRejectsDifferentPasswordConfirmation() {
        UsuarioSenhaDTO senhaDTO = new UsuarioSenhaDTO(NEW_PASSWORD, "outra2", CURRENT_PASSWORD);
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuario));

        PasswordInvalidException exception = assertThrows(
                PasswordInvalidException.class,
                () -> usuarioService.updatePassword(USER_ID, senhaDTO)
        );

        assertEquals("A nova senha e a confirmação não conferem", exception.getMessage());
        assertEquals(CURRENT_PASSWORD, usuario.getPassword());
        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    void updatePasswordRejectsUnknownUser() {
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> usuarioService.updatePassword(
                        USER_ID,
                        new UsuarioSenhaDTO(NEW_PASSWORD, NEW_PASSWORD, CURRENT_PASSWORD)
                )
        );

        assertEquals("Usuário com id=1 não encontrado", exception.getMessage());
        verify(usuarioRepository, never()).save(usuario);
    }
}
