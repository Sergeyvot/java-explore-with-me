package ru.practicum.compilation;

import lombok.experimental.UtilityClass;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;

@UtilityClass
public class CompilationMapperUtil {

    public Compilation toCompilation(NewCompilationDto compilationDto) {
        Compilation.CompilationBuilder compilation = Compilation.builder();

        compilation.pinned(compilationDto.getPinned() != null ? compilationDto.getPinned() : false);
        compilation.title(compilationDto.getTitle());

        return compilation.build();
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        CompilationDto.CompilationDtoBuilder compilationDto = CompilationDto.builder();

        compilationDto.id(compilation.getId());
        compilationDto.pinned(compilation.getPinned());
        compilationDto.title(compilation.getTitle());

        return compilationDto.build();
    }
}
