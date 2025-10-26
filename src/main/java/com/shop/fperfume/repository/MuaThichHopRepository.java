package com.shop.fperfume.repository;

import com.shop.fperfume.entity.MuaThichHop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MuaThichHopRepository extends JpaRepository<MuaThichHop,Long> {
}
