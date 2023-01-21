# Data

This is where the data is expected to be placed from the parser.

The data here should be downloaded from [this site](https://icandecide.org/v-safe-data/), and decompressed into five CSV files:

- consolidated_vaccinations[1].csv- Consolidated_health_checkin_u3[1].csv- consolidated_health_checkin.csv- consolidated_race_ethnicity[1].csv- consolidated_registrants[1].csv

(These are the file names that the .zip files decompress to. Note: the `consolidated_health_checkin.csv` file is rediculously large and the default Mac archival utility barfs on the file. I had to use [The Unarchiver](https://apps.apple.com/us/app/the-unarchiver/id425424353?mt=12) to successfully decompress the file.)

Note that the files are not included in this GitHub repository because of their size.

----

## File structure

It appears the `Consolidated_health_checkin_u3[1].csv` file is an extract from the much larger `consolidated_health_checkin.csv`--and I'm leery of trusting how someone else bent the data--so that file is ignored.

All of the records in this database seem to key off the `REGISTRANT_CODE` field, which appears to be an anonymized individual. So if we want to track the progress of someone's health after receiving the vaccine--which is my primary interest here--the first thing we need to do is scrape the records and accumulate statistics on a per-registrant basis.

There are four files we are interested in. 

Those are:

### consolidated_registrants[1].csv

Fields

- REGISTRANT\_CODE
- ZIP\_CODE
- SEX
- REGISTERED\_DATE
- TIME\_ZONE
- RELATIONSHIP
- GUARDIAN\_REGISTRANT\_CODE

It's a shame we don't have age data associated with each of these, because that would allow us to figure out the all important question "at what age does the cost of taking the vaccine exceed the benefit." That is, we know for people who are overweight and over 50 (as I am), the potential risk of catching COVID-19 is relatively high. 

But if you're a normal weight, in relatively good health, and under 40, the chances COVID-19 hits you harder than (say) the common cold (or is even asymptomatic) is a hell of a lot higher than for me.

However, we certainly can characterize the severity of reaction to the shot regardless.

That severity, by the way, is tracked in 

### data/consolidated\_health\_checkin.csv

- SURVEY\_STATIC\_ID
- REGISTRANT\_CODE
- RESPONSE\_ID
- STARTED\_ON
- STARTED\_ON\_TIME
- DAYS\_SINCE
- ABDOMINAL\_PAIN
- CHILLS
- DIARRHEA
- FATIGUE
- FEELING\_TODAY
- FEVER
- HAD\_SYMPTOMS
- HEADACHE
- HEALTH\_IMPACT
- HEALTH\_NOW
- HEALTH\_NOW\_COMPARISON
- VACCINE\_CAUSED\_HEALTH\_ISSUES
- HEALTHCARE\_VISITS
- ITCHING
- JOINT\_PAINS
- MUSCLE\_OR\_BODY\_ACHES
- NAUSEA
- PAIN
- PREGNANT
- PREGNANCY\_TEST
- RASH\_OUTSIDE\_INJECTION\_SITE
- REDNESS
- SITE\_REACTION
- SWELLING
- SYSTEMIC\_REACTION
- TEMPERATURE\_CELSIUS
- TEMPERATURE\_FAHRENHEIT
- TEMPERATURE\_READING
- TESTED\_POSITIVE
- TESTED\_POSITIVE\_DATE
- VOMITING
- DURATION\_MINS
- PREFERRED\_LANGUAGE

This contains 144,856,043 records, individual reports made to V-Safe. The first six records appear to contain:

- SURVEY\_STATIC\_ID
- REGISTRANT\_CODE
- RESPONSE\_ID
- STARTED\_ON
- STARTED\_ON\_TIME
- DAYS\_SINCE


Possible values for the health-related fields are:

#### SURVEY\_STATIC\_ID

This appears to be an identifier for the type of V-Safe report, containing things like "vsafe-1-6-daily-dose2-under-3", which appears to be the reason for the prompt. (Like "you've just had your second dose. How do you feel today?")

#### RESPONSE\_ID

A unique row identifier.

#### STARTED\_ON

The date this report was made.

#### STARTED\_ON\_TIME

The time this report was made

#### DAYS\_SINCE

This appears to be the number of days since the last time this registrant made a report; 0 if this is the first report.

#### Health reactions

The fields ABDOMINAL_PAIN, CHILLS, DIARRHEA, FATIGUE, HEADACHE, ITCHING, JOINT_PAINS, MUSCLE_OR_BODY_ACHES, NAUSEA, PAIN, RASH_OUTSIDE_INJECTION_SITE, REDNESS, SWELLING and VOMITING contain the possible values "", "Mild", "Moderate" or "Severe".

The fields FEVER, HAD_SYMPTOMS, VACCINE_CAUSED_HEALTH_ISSUES and TESTED_POSITIVE have the values "Yes", "No" or "", which I presume means "No".

The fields PREGNANT and PREGNANCY_TEST flag pregnancy; the former is "", "No", "I don't know" or "Yes"; the later, "", "No" or "Yes".

For TESTED_POSITIVE there is also an accompanying field TESTED_POSITIVE_DATE which gives the date that person tested positive.

#### Health status

The field HEALTH_NOW is one of "", "Excellent", "Good", "Fair" or "Poor", with the fields HEALTH_NOW_COMPARISON one of "", "Worse", "About the same" or "Better".

#### Flag fields

The field SITE_REACTION contains a list of one or more colon-separated values

- Pain
- Redness
- Swelling
- Itching

The field can also contain "" or "None"

the field SYSTEMIC_REACTION contains a "" blank field, "None", or one or more colon-separated values:

- Abdominal pain
- Chills
- Diarrhea
- Fatigue or tiredness
- Headache
- Joint pains
- Muscle or body aches
- Nausea
- Rash, not including the immediate area around the injection site
- Vomiting

The field HEALTHCARE_VISITS contains a "" blank field or one or more colon-separated values

- Emergency room or emergency department visit
- Hospitalization
- Outpatient clinic or urgent care clinic visit
- Telehealth, virtual health, or email health consultation


#### Temperature

TEMPERATURE_CELSIUS and TEMPERATURE_FAHRENHEIT contain the temperature the person reported in celsius or fahrenheit.

The field TEMPERATURE_READING contains one of blank ("") or:

- Yes - in degrees Fahrenheit
- Yes - in degrees Celsius
- No - I didn't take their temperature
- No - I didn't take my temperature
- No - I don't remember their reading
- No - I don't remember my reading

### Demographic Data

Demographic data is contained in `consolidated_race_ethnicity[1].csv`:

- REGISTRANT\_CODE
- RACE
- ETHNICITY

### Vaccine Data

Vaccine data is contained in `consolidated_vaccinations[1].csv`:

- REGISTRANT\_CODE
- MANUFACTURER
- DOSE\_NUMBER
- VACCINATION\_DATE
- COADMINISTERED
- COADMINISTERED\_VAX


