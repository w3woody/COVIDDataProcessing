# COVID Data Processing

So there is a [web site](https://icandecide.org/v-safe-data) floating around out there, pushed out there on Twitter, which is making the apparent claim that 7.7% of people who received the vaccine had to seek medical assistance because of receiving the vaccine.

Which struck me as... odd.

So I went ahead and downloaded the data from their web site, and wrote a little Java code to parse the data and see if their numbers on the web site are supported by the data provided, and if the numbers actually made any sense.

In the interest of full and complete disclosure, the code I hacked together one Saturday afternoon (and trust me, it's not my best work, but I'm not going to make a career out of parsing this data--unless someone wants to pay me to do that...) to comb the data is here.

It's a bad rough start at trying to understand the structure of the data posted by that web site, and to tease out certain results.

----

Things I'd do if I were rewriting this:

1. Rewrite the code to do a pre-processing step to convert the total reports into binary for faster loading. (The sketch of this is in Report2.java.)
2. Rewrite the caching code to keep in-memory data loaded and processed.
3. More reports! I can see doing things like scanning the history of a participant to see things like "how long did it take for self-reported site-injection symptoms to subside."

----

First, this code is published without any claims of copyright. Feel free to download the code and manipulate it in any way. The only thing I ask is if you use this code for your own advocacy, leave my damned name out of all of this. There has been plenty of terrible statistical manipulation to promote advocacy pretending to be reproducable science that I've had my fucking fill--and I don't want to be a part of it.

For me, it's "strong convictions, loosely held." Meaning from what I know and I see, I have very strong convictions about a lot of things--including the safety and efficacy of the COVID vaccine, including the necessity for *some* age groups, especially with certain co-morbidities, to get vaccinated. (Me: as a 57 year old overweight male, I have all the shots: the original two Moderna shots in the original sequence, the later booster--after evidence seemed to suggest the first two shots should have been spaced out farther apart, and the reformulated Pfizer booster shot. Because as an overweight 57 year old male, I am at much higher risk of an adverse reaction to COVID than if, say, I was a 20-year old female with an optimal BMI of 22.)

----

## Limitations of the data

But a few words about the V-Safe data.

(1) The data **IS NOT** the "gold standard" of data sets. It's a huge data set, granted, with some 9.5 million participants making over 142 million self-reports.

But it is *self-reported* data. And that has limitations: basically, if you feel fine, the chances of you dropping out of the program is likely to be higher than if you have a number of symptoms you're reporting.

(2) The data **DOES NOT** provide any information about pre-existing conditions. Nor does it provide us with age data; only demographic data. (That is, race and ethnicity and gender.) Because we know COVID symptoms strongly correlate with age, that this data set is missing age data prevents us from answering the question "at what age, if any, does taking the vaccine create a higher risk of health-related problems than simply catching COVID-19"?

(We know for a fact that COVID-19's health impacts on those under 20 has been minor. So it's not inconceivable that the vaccine may actually cause more problems for someone sufficiently young enough than getting infected. **Unfortunately too many activists on both sides of the debate have muddied the water so thoroughly that we cannot even ask the question without pissing people off.** And it's stupid--because there are plenty of vaccines where we know vaccinating certain cohorts makes little sense, but where the vaccine is damned near necessary if you are a member of certain groups of people. Like the Shingles vaccine, which is only administered when you are older.)

(3) The data **DOES NOT** differentiate between those who seek medical help because of the vaccine, and those who seek it for other reasons. And--the key part here--**we do not have a similar data set for an unvaccinated cohort.** This becomes important because as certain claims are made--like the top-line report that 7.7% of people who received the vaccine sought medical help--we cannot know if this is good or bad.

That is, there isn't another group of people who didn't receive the vaccine we can compare to, and say, for example, "well for those not vaccinated, 8.5% sought medical help."

And that allows the advocates to make the *unfounded* assumption that the unvaccinated cohort **never sought medical help.**

That is what you're thinking when you read "7.7% of people who got vaccinated sought medical help", right? That if they didn't get vaccinated, that number would be 0?

----

So for these reasons, the V-Safe data set is useless in determining how harmful the vaccine is--unless we have other numbers we can compare them to.

And that's not in this data set.

----

## Results

All this said, I do have a few results. 

This is the actual output of the code in this repository, if you set it all up. (The results are here so if you can't download the code, at least you know where this is going. But if you can--do so. Don't trust some random idiot on the Internet to tell you what's what!)

(1) When scanning the full data set, the Java code in this GitHub repository reports the following:

Of 9,552,127 unique registrants found in the file `consolidated_health_checkin.csv` (which differs from the 10,108,273 "total individual users" described in the web link above where I got this data, which I presume is the number of rows in the `consolidated_registrants[1].csv` registrants file--a file which also includes guardians, apparently):

The number of people who report seeking health care are:

    Total registrants: 9552127
    Any:               679277   7.111%
    ER Visits:         155362   1.626%
    Hospital Visits:   71911    0.753%
    Outpatient Visits: 397745   4.164%
    Telehealth Visits: 264324   2.767%

So even their top-line scary number of 7.7% seems wrong!

(This is the count of the number of unique participants who report any contact with health care--that is, where `HEALTHCARE_VISITS` reports a value that is not a blank, "None" or "N/A". I suspect their web site is not differentiating by type of visit--which is why they may be counting "None" as a health care visit.)

(Quick note: the "Any" total is less than the sum of the different types of visits, because some individuals reported more than one mode of visit--such as getting a telehealth visit, then later visiting an ER.)

(2) More interestingly, if we count only those who report a health care visit after asserting the vaccine caused them health related symptoms--that is, at some point in their history they said "yes, the vaccine is causing me problems", then later--at any time--sought medical attention, those numbers drop drastically:

    Health care interactions by people after vaccine flag set:
    Any:               65955   0.690%
    ER Visits:         18486   0.194%
    Hospital Visits:   8266    0.087%
    Outpatient Visits: 44226   0.463%
    Telehealth Visits: 24524   0.257%

(This is the count of the number of unique participants where `HEALTHCARE_VISITS` is set to a value not blank, "None" or "N/A", where, at some point on or before the report, the flag `VACCINE_CAUSED_HEALTH_ISSUES` was set to "Yes". The correlation, built in Report2.java, sorted the reports for each participant in date order first.)

**This is hardly the top-line number where 7.7% of people sought medical assistance because of the vaccine.**


----

Of course I have better things to do with my life than debunk bullshit.

But here we are, and here's the GitHub repo with the code, so you can try to reproduce the numbers yourself, and perhaps mine the data further if you so choose.