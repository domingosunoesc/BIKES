package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.dto.UsuarioSenhaDTO;
import br.edu.ifc.bikes.dto.mapper.UsuarioMapper;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.PasswordInvalidException;
import br.edu.ifc.bikes.exception.UsernameUniqueViolationException;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioResponseDTO create(UsuarioRequestDTO usuarioRequestDTO) {
        try {
            Usuario usuario = usuarioMapper.toUsuario(usuarioRequestDTO);
            return usuarioMapper.toResponse(usuarioRepository.save(usuario));
        }catch (RuntimeException ex){

            throw new UsernameUniqueViolationException(String.format("O nome do usuário '%s' já existe", usuarioRequestDTO.username()));
        }
    }

    @Transactional()
    public UsuarioResponseDTO getById(Long id) {
        return usuarioMapper.toResponse(usuarioRepository.findById(id).orElseThrow(
                ()->new EntityNotFoundException(String.format("Usuário com id=%d não encontrado", id))
        ));
    }

    @Transactional
    public UsuarioResponseDTO updatePassword(Long id, UsuarioSenhaDTO usuarioSenhaDTO) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Usuário com id=%d não encontrado", id))
        );

        if (!usuario.getPassword().equals(usuarioSenhaDTO.senhaAtual())) {
            throw new PasswordInvalidException("A senha atual não confere");
        }
        if (!usuarioSenhaDTO.novaSenha().equals(usuarioSenhaDTO.confirmarSenha())) {
            throw new PasswordInvalidException("A nova senha e a confirmação não conferem");
        }

        usuario.setPassword(usuarioSenhaDTO.novaSenha());
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    public List<UsuarioResponseDTO> getAll(){
        return usuarioMapper.toResponse(usuarioRepository.findAll());
    }
}