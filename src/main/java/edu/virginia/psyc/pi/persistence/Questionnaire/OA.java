package edu.virginia.psyc.pi.persistence.Questionnaire;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import edu.virginia.psyc.pi.domain.DoNotDelete;
import edu.virginia.psyc.pi.domain.Session;
import edu.virginia.psyc.pi.persistence.ParticipantDAO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This Oasis questionnaire is used over time in the study to determine
 * participant progress.  So while it can be exported through the admin
 * panel, the data should not be deleted.  To help make things a little
 * safer, we obscure the names of the fields while the data is stored in
 * the database.
 */
@Entity
@Table(name="OA")
@Data
@EqualsAndHashCode(callSuper = true)
@DoNotDelete
public class OA extends QuestionnaireData implements Comparable<OA> {

    private static final Logger LOG = LoggerFactory.getLogger(OA.class);

    @ManyToOne
    @JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
    @JsonIdentityReference(alwaysAsId=true) // otherwise first ref as POJO, others as id
    protected ParticipantDAO participantDAO;

    public static int NO_ANSWER = 555;
    public static final int MAX_SCORE = 4;

    @Column(name="AXF")
    private int anxious_freq;
    @Column(name="AXS")
    private int anxious_sev;
    @Column(name="AVO")
    private int avoid;
    @Column(name="WRK")
    private int interfere;
    @Column(name="SOC")
    private int interfere_social;

    public OA(int anxious_freq, int anxious_sev, int avoid, int interfere, int interfere_social) {
        this.anxious_freq=anxious_freq;
        this.anxious_sev = anxious_sev;
        this.avoid = avoid;
        this.interfere = interfere;
        this.interfere_social = interfere_social;
        this.date = new Date();
    }

    public OA(){}

    public double score() {

        int sum = 0;
        double total = 0.0;

        if(anxious_freq != NO_ANSWER) { sum += anxious_freq; total++;}
        if(anxious_sev != NO_ANSWER) { sum += anxious_sev; total++;}
        if(avoid != NO_ANSWER) { sum += avoid; total++;}
        if(interfere != NO_ANSWER) { sum += interfere; total++;}
        if(interfere_social != NO_ANSWER) { sum += interfere_social; total++;}

        if (total == 0) return 0;
        return(sum / total) * 5;
    }

    public boolean atRisk(OA original) {
      if(original.score() != 0) {
        return (score() / original.score()) > 1.5;
      } else { return false; }
    }

    @Override
    public int compareTo(OA o) {
        return date.compareTo(o.date);
    }

}
