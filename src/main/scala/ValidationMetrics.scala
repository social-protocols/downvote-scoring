object ValidationMetrics {
  val averageTopPageScoreSum =
    14454.4163989552 // select avg(sum_score) from (select tick, sum(score) as sum_score from dataset where toprank <= 90 and samplingWindow = 3 group by tick);

  val averageNewPageScoreSum =
    403.922899090281 // select avg(sum_score) from (select tick, sum(score) as sum_score from dataset where newrank <= 90 and samplingWindow = 3 group by tick)

  val averageTopPageAgeAvg =
    68223.2530792646 // select avg(age) from (select avg(sampleTime - submissiontime) as age from dataset where toprank <= 90 and samplingWindow = 3 group by tick)
}
