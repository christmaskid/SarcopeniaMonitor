from django.db import models
from django.contrib.postgres.fields import ArrayField
from datetime import date

class UserRecord(models.Model):
    recordID = models.BigAutoField(primary_key=True)
    recordDate = models.CharField(max_length=16)
    recordTime = models.CharField(max_length=16)
    height = models.FloatField(blank=True, null=True)
    weight = models.FloatField(blank=True, null=True)
    bmi = models.FloatField(blank=True, null=True)
    sbp = models.FloatField(blank=True, null=True)
    dbp = models.FloatField(blank=True, null=True)
    mealImages = models.JSONField(default=list, blank=True)  # Store meal images as a list of objects

    def calculate_age(self):
        """
        Calculate the age of the user based on the birthDate in the related Questionnaire.
        """
        questionnaire = Questionnaire.objects.filter(id=self.recordID).first()
        if questionnaire and questionnaire.birthDate:
            today = date.today()
            return today.year - questionnaire.birthDate.year - (
                (today.month, today.day) < (questionnaire.birthDate.month, questionnaire.birthDate.day)
            )
        return None

    def get_meal_image_uris(self):
        """
        Return a list of URIs for all associated meal images.
        """
        return [meal_image['image_uri'] for meal_image in self.meal_images]

class Questionnaire(models.Model):
    birthDate = models.DateField(blank=True, null=True)
    gender = models.IntegerField(blank=True, null=True)
    smoking = models.IntegerField(blank=True, null=True)
    a2_1 = models.IntegerField(blank=True, null=True)
    a2_2 = models.IntegerField(blank=True, null=True)
    b1_2 = models.IntegerField(blank=True, null=True)
    b2_2 = models.IntegerField(blank=True, null=True)
    b9 = models.IntegerField(blank=True, null=True)
    c15 = models.IntegerField(blank=True, null=True)
    d1 = models.IntegerField(blank=True, null=True)
    d2 = models.IntegerField(blank=True, null=True)
    d3 = models.IntegerField(blank=True, null=True)
    e1 = models.IntegerField(blank=True, null=True)
    meal_status = models.IntegerField(blank=True, null=True)

class PhysicalTest(models.Model):
    gripStrengthData = models.FloatField(blank=True, null=True)
    gaitSpeedData = models.FloatField(blank=True, null=True)

class Prediction(models.Model):
    asmiPrediction = models.FloatField(blank=True, null=True)
    handGripPrediction = models.FloatField(blank=True, null=True)
    sarcopeniaStatus = models.IntegerField(blank=True, null=True)
    gaitSpeedData = models.FloatField(blank=True, null=True)
    standUpData = models.FloatField(blank=True, null=True)