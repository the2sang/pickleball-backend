package com.pickleball.repository;

import com.pickleball.entity.QAccount;
import com.pickleball.entity.QMember;
import com.pickleball.entity.QReservation;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ActivePlayerRow> findActivePlayerRows(Long courtId, LocalDate gameDate) {
        QReservation r = QReservation.reservation;
        QAccount a = QAccount.account;
        QMember m = QMember.member;

        return queryFactory
                .select(Projections.constructor(
                        ActivePlayerRow.class,
                        r.timeSlot,
                        r.id,
                        a.username,
                        a.name,
                        m.nicName,
                        m.gameLevel,
                        m.duprPoint,
                        m.sex,
                        r.createDate))
                .from(r)
                .join(r.account, a)
                .leftJoin(m).on(m.accountId.eq(a.id))
                .where(
                        r.courtId.eq(courtId),
                        r.gameDate.eq(gameDate),
                        r.cancelYn.ne("Y"),
                        r.approvalStatus.eq("APPROVED"))
                .orderBy(r.timeSlot.asc(), r.createDate.asc())
                .fetch();
    }

    @Override
    public List<ActivePlayerRow> findActivePlayerRows(Long courtId, LocalDate gameDate, String timeSlot) {
        QReservation r = QReservation.reservation;
        QAccount a = QAccount.account;
        QMember m = QMember.member;

        return queryFactory
                .select(Projections.constructor(
                        ActivePlayerRow.class,
                        r.timeSlot,
                        r.id,
                        a.username,
                        a.name,
                        m.nicName,
                        m.gameLevel,
                        m.duprPoint,
                        m.sex,
                        r.createDate))
                .from(r)
                .join(r.account, a)
                .leftJoin(m).on(m.accountId.eq(a.id))
                .where(
                        r.courtId.eq(courtId),
                        r.gameDate.eq(gameDate),
                        r.timeSlot.eq(timeSlot),
                        r.cancelYn.ne("Y"),
                        r.approvalStatus.eq("APPROVED"))
                .orderBy(r.createDate.asc())
                .fetch();
    }
}
