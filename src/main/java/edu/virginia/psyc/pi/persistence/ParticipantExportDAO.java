package edu.virginia.psyc.pi.persistence;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.virginia.psyc.pi.domain.DoNotDelete;
import edu.virginia.psyc.pi.domain.Exportable;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by dan on 3/24/16.
 * Data about a Participant that can be exported.
 */
@Data
@Entity
@Table(name="participant")
@Exportable
@DoNotDelete
public class ParticipantExportDAO {

    @Id private long id;
    private String theme;
    private String study;
    private String cbmCondition;
    private String prime;
    private boolean admin;
    private boolean emailOptout;
    private boolean active;
    private boolean increase30;
    private String riskSession;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="EEE, dd MMM yyyy HH:mm:ss Z", timezone="EST")
    private Date   lastLogin;
    private String currentSession;
    private boolean receiveGiftCards;
}
