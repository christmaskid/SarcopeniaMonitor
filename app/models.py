from django.db import models
from django.utils.timezone import now
from django.core.validators import MinValueValidator, MaxValueValidator

RECORD_FIELDS = [
    "recordID", "timestamp",  "recordDate", "recordTime",
    "height", "weight", "bmi", 
    "sbp", "dbp"
]

class UserRecord(models.Model):
    recordID = models.BigAutoField(primary_key=True)
    recordDate = models.CharField(max_length=16)
    recordTime = models.CharField(max_length=16)
    timestamp = models.DateTimeField(default=now)

    height = models.FloatField(blank=True, null=True)
    weight = models.FloatField(blank=True, null=True)
    bmi = models.FloatField(blank=True, null=True)
    
    sbp = models.FloatField(blank=True, null=True)
    dbp = models.FloatField(blank=True, null=True)

class Questionnaire(models.Model):
    recordID = models.BigAutoField(primary_key=True)
    timestamp = models.DateTimeField(default=now)
    
    smoking = models.IntegerField(
        blank=True, null=True,
        validators=[MinValueValidator(0), MaxValueValidator(2)]
    )
    a2 = models.IntegerField(blank=True, null=True)
    a3 = models.IntegerField(blank=True, null=True)
    b2 = models.IntegerField(blank=True, null=True)
    b2_1 = models.IntegerField(blank=True, null=True)
    b2_2 = models.IntegerField(blank=True, null=True)
    b4_2 = models.IntegerField(blank=True, null=True)
    c2 = models.IntegerField(blank=True, null=True)
    c11 = models.IntegerField(blank=True, null=True)
    e2 = models.IntegerField(blank=True, null=True)
    e4 = models.IntegerField(blank=True, null=True)
    ad8_a3 = models.IntegerField(blank=True, null=True)