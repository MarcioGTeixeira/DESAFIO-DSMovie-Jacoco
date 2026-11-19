package com.devsuperior.dsmovie.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ScoreServiceTests {

    @InjectMocks
    private ScoreService service;

    @Mock
    private UserService userService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ScoreRepository scoreRepository;

    private Long existingMovieId;
    private Long nonExistingMovieId;

    private ScoreDTO scoreDTO;
    private MovieEntity movie;

    @BeforeEach
    void setUp() {

        existingMovieId = 1L;
        nonExistingMovieId = 1000L;

        movie = new MovieEntity();
        movie.setId(existingMovieId);
        movie.setTitle("Filme Teste");
        movie.setScore(0.0);
        movie.setCount(0);

        scoreDTO = new ScoreDTO(existingMovieId, 5.0);
    }

    @Test
    public void saveScoreShouldReturnMovieDTO() {

        ScoreEntity existingScore = new ScoreEntity();
        existingScore.setValue(4.0);

        movie.getScores().add(existingScore);

        when(userService.authenticated())
                .thenReturn(new UserEntity());

        when(movieRepository.findById(existingMovieId))
                .thenReturn(Optional.of(movie));

        when(scoreRepository.saveAndFlush(any(ScoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(movieRepository.save(any(MovieEntity.class)))
                .thenReturn(movie);

        MovieDTO result = service.saveScore(scoreDTO);

        assertNotNull(result);
    }

    @Test
    public void saveScoreShouldReturnMovieDTOWhenMovieHasNoScores() {

        movie.getScores().clear();

        when(userService.authenticated())
                .thenReturn(new UserEntity());

        when(movieRepository.findById(existingMovieId))
                .thenReturn(Optional.of(movie));

        when(scoreRepository.saveAndFlush(any(ScoreEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(movieRepository.save(any(MovieEntity.class)))
                .thenReturn(movie);

        MovieDTO result = service.saveScore(scoreDTO);

        assertNotNull(result);
    }

    @Test
    public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {

        when(userService.authenticated())
                .thenReturn(new UserEntity());

        when(movieRepository.findById(nonExistingMovieId))
                .thenReturn(Optional.empty());

        scoreDTO.setMovieId(nonExistingMovieId);

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.saveScore(scoreDTO)
        );
    }
}