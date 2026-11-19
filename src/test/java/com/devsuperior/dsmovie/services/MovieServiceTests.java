package com.devsuperior.dsmovie.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTests {

    @InjectMocks
    private MovieService service;

    @Mock
    private MovieRepository repository;

    private Long existingId;
    private Long nonExistingId;
    private Long dependentId;

    private MovieEntity movie;
    private MovieDTO dto;

    private PageImpl<MovieEntity> page;

    @BeforeEach
    void setUp() {

        existingId = 1L;
        nonExistingId = 1000L;
        dependentId = 2L;

        movie = new MovieEntity(
                existingId,
                "Avatar",
                4.5,
                10,
                "https://img.com/avatar.jpg");

        dto = new MovieDTO(
                existingId,
                "Avatar",
                4.5,
                10,
                "https://img.com/avatar.jpg");

        page = new PageImpl<>(Arrays.asList(movie));
    }

    @Test
    public void findAllShouldReturnPagedMovieDTO() {

        Pageable pageable = PageRequest.of(0, 10);

        Mockito.when(repository.searchByTitle("", pageable))
                .thenReturn(page);

        Page<MovieDTO> result = service.findAll("", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void findByIdShouldReturnMovieDTOWhenIdExists() {

        Mockito.when(repository.findById(existingId))
                .thenReturn(Optional.of(movie));

        MovieDTO result = service.findById(existingId);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Mockito.when(repository.findById(nonExistingId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(nonExistingId));
    }

    @Test
    public void insertShouldReturnMovieDTO() {

        Mockito.when(repository.save(any(MovieEntity.class)))
                .thenReturn(movie);

        MovieDTO result = service.insert(dto);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
    }

    @Test
    public void updateShouldReturnMovieDTOWhenIdExists() {

        Mockito.when(repository.getReferenceById(existingId))
                .thenReturn(movie);

        Mockito.when(repository.save(any(MovieEntity.class)))
                .thenReturn(movie);

        MovieDTO result = service.update(existingId, dto);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Mockito.when(repository.getReferenceById(nonExistingId))
                .thenThrow(EntityNotFoundException.class);

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.update(nonExistingId, dto));
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {

        Mockito.when(repository.existsById(existingId))
                .thenReturn(true);

        Mockito.doNothing()
                .when(repository)
                .deleteById(existingId);

        assertDoesNotThrow(
                () -> service.delete(existingId));
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Mockito.when(repository.existsById(nonExistingId))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.delete(nonExistingId));
    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId() {

        Mockito.when(repository.existsById(dependentId))
                .thenReturn(true);

        Mockito.doThrow(DataIntegrityViolationException.class)
                .when(repository)
                .deleteById(dependentId);

        assertThrows(
                DatabaseException.class,
                () -> service.delete(dependentId));
    }
}