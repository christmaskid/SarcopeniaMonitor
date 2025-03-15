from django.db import models
from django.utils.timezone import now

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