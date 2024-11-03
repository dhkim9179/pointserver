package com.example.pointserver.use.repository;

public interface UseRepositoryCustom {
    Integer findBalance(long memberId);
    void increaseBalance(long memberId, int point);
    void decreaseBalance(long memberId, int point);
}
