package ru.proitr.tru.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.proitr.tru.tables.pojos.NFtp_223TruIp;
import ru.proitr.tru.tables.pojos.NFtp_223TruMsp;

import static ru.proitr.tru.Tables.N_PARTICIPANT;
import static ru.proitr.tru.Tables.N_FTP_223_TRU_MSP;
import static ru.proitr.tru.Tables.N_FTP_223_TRU_IP;


@Repository
public class TruMspRepository  {

    @Autowired
    private DSLContext dslContext;

    public boolean isEaistCustomer(String inn, String kpp) {
        return dslContext.selectCount()
                .from(N_PARTICIPANT)
                .where(N_PARTICIPANT.INN.eq(inn).and(N_PARTICIPANT.KPP.eq(kpp).and(N_PARTICIPANT.DELETED_DATE.isNull())))
                .fetchOne(0, Long.class) > 0;
    }

    @Nullable
    public NFtp_223TruMsp getActualMsp(String inn, String kpp) {
        return dslContext.select(N_FTP_223_TRU_MSP.fields())
                .from(N_FTP_223_TRU_MSP)
                .where(N_FTP_223_TRU_MSP.CUSTOMER_INN.eq(inn).and(N_FTP_223_TRU_MSP.CUSTOMER_KPP.eq(kpp))
                        .and(N_FTP_223_TRU_MSP.DELETED_DATE.isNull()))
                .fetch()
                .stream()
                .map(record -> new NFtp_223TruMsp(record.into(NFtp_223TruMsp.class)))
                .findFirst().orElse(null);
    }

    @Nullable
    public NFtp_223TruIp getActualIp(String inn, String kpp) {
        return dslContext.select(N_FTP_223_TRU_IP.fields())
                .from(N_FTP_223_TRU_IP)
                .where(N_FTP_223_TRU_IP.CUSTOMER_INN.eq(inn).and(N_FTP_223_TRU_IP.CUSTOMER_KPP.eq(kpp))
                        .and(N_FTP_223_TRU_IP.DELETED_DATE.isNull()))
                .fetch()
                .stream()
                .map(record -> new NFtp_223TruIp(record.into(NFtp_223TruIp.class)))
                .findFirst().orElse(null);
    }
}
