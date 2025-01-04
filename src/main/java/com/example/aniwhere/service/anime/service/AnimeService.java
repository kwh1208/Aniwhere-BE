package com.example.aniwhere.service.anime.service;

import com.example.aniwhere.repository.casting.repository.CastingRepository;
import com.example.aniwhere.repository.review.repository.ReviewRepository;
import com.example.aniwhere.domain.anime.Anime;
import com.example.aniwhere.domain.anime.dto.AnimeDTO.*;
import com.example.aniwhere.repository.anime.repository.AnimeRepository;
import com.example.aniwhere.domain.category.Category;
import com.example.aniwhere.global.error.ErrorCode;
import com.example.aniwhere.global.error.exception.ResourceNotFoundException;

import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnimeService {
    private final AnimeRepository animeRepository;
    private final CastingRepository castingRepository;
    private final ReviewRepository reviewRepository;

    public Map<String, List<QuarterAnimeResponseDTO>> getAnimeByYearAndQuarter(int year, int quarter) {
        List<Anime> animes = animeRepository.findByYearAndQuarter(year, quarter);


        List<String> weekdays = Arrays.asList("월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일");

        // 요일별로 response
        Map<String, List<QuarterAnimeResponseDTO>> groupedAnimes = weekdays.stream()
                .collect(Collectors.toMap(day -> day, day -> new ArrayList<>(), (a, b) -> a, LinkedHashMap::new));

        // 애니메이션 요일별로
        animes.stream()
                .filter(anime -> weekdays.contains(anime.getWeekday())) // 요일이 없는 경우 고려
                .map(this::convertToDTO)
                .forEach(anime -> groupedAnimes.get(anime.getWeekday()).add(anime));

        return groupedAnimes;
    }

    //Anime response로 변환
    private QuarterAnimeResponseDTO convertToDTO(Anime anime) {
        return QuarterAnimeResponseDTO.builder()
                .animeId(anime.getAnimeId())
                .title(anime.getTitle())
                .poster(anime.getPoster())
                .weekday(anime.getWeekday())
                .build();
    }

    public AnimeResponseDTO getAnimeById(long animeId) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 애니메이션에 대한 정보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_USER));

        List<AnimeResponseDTO.CastingDTO> castings = castingRepository.findCastingByAnime_animeId(animeId).stream()
                .map(casting -> AnimeResponseDTO.CastingDTO.builder()
                        .castingId(casting.getCastingId())
                        .characterName(casting.getCharacterName())
                        .characterDescription(casting.getCharacterDescription())
                        .voiceActorName(casting.getVoiceActor().getName()) // 성우 이름을 포함한 캐스팅 정보
                        .build())
                .collect(Collectors.toList());

        List<AnimeResponseDTO.ReviewDTO> reviews = reviewRepository.findByAnime_AnimeId(animeId).stream()
                .map(review -> AnimeResponseDTO.ReviewDTO.builder()
                        .reviewId(review.getReviewId())
                        .userId(review.getUser().getId().toString())
                        .rating(review.getRating())
                        .content(review.getContent())
                        .createdAt(review.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return AnimeResponseDTO.builder()
                .animeId(anime.getAnimeId())
                .title(anime.getTitle())
                .director(anime.getDirector())
                .characterDesign(anime.getCharacterDesign())
                .musicDirector(anime.getMusicDirector())
                .animationDirector(anime.getAnimationDirector())
                .script(anime.getScript())
                .producer(anime.getProducer())
                .studio(anime.getStudio())
                .releaseDate(anime.getReleaseDate())
                .endDate(anime.getEndDate())
                .episodes(anime.getEpisodes())
                .runningTime(anime.getRunningTime())
                .status(anime.getStatus())
                .trailer(anime.getTrailer())
                .description(anime.getDescription())
                .poster(anime.getPoster())
                .airingQuarter(anime.getAiringQuarter())
                .isAdult(anime.getIsAdult())
                .duration(anime.getDuration())
                .weekday(anime.getWeekday())
                .anilistId(anime.getAnilistId())
                .categories(anime.getCategories().stream()
                        .map(Category::getCategoryName) // Category ID만 추출
                        .collect(Collectors.toSet()))
                .castings(castings)
                .reviews(reviews)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<Integer, List<WeekdayAnimeDTO>> getAnimeWeekdayList(Integer year, Integer quarter) {
        // 기본값 설정: 현재 연도와 분기
        if (year == null) {
            year = Year.now().getValue();
        }
        if (quarter == null) {
            quarter = (LocalDate.now().getMonthValue() - 1) / 3 + 1; // 현재 월 기준 분기 계산
        }
        // 데이터 조회
        Map<Integer, List<Anime>> groupedByWeekday = animeRepository.findAllGroupedByWeekday(year, quarter);

        return groupedByWeekday.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(anime -> WeekdayAnimeDTO.builder()
                                        .animeId(anime.getAnimeId())
                                        .title(anime.getTitle())
                                        .poster(anime.getPoster())
                                        .weekday(anime.getWeekday())
                                        .airingQuarter(anime.getAiringQuarter())
                                        .releaseDate(anime.getReleaseDate().getYear())
                                        .endDate(anime.getEndDate().getYear())
                                        .build())
                                .collect(Collectors.toList()),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }
}

