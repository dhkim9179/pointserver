package com.example.pointserver.earn.repository;

public interface EarnRepositoryCustom {
    Integer findBalance(long memberId);
    void increaseBalance(long memberId, int point);
    void decreaseBalance(long memberId, int point);
}
