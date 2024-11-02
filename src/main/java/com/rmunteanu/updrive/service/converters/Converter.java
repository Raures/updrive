package com.rmunteanu.updrive.service.converters;

public interface Converter<D, M> {
    M fromDTO(D dto);
    D toDTO(M entity);
}

