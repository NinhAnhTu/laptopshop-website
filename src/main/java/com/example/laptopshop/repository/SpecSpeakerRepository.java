package com.example.laptopshop.repository;

import com.example.laptopshop.entity.SpecMouse;
import com.example.laptopshop.entity.SpecSpeaker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecSpeakerRepository extends JpaRepository<SpecSpeaker, Long> {
}
