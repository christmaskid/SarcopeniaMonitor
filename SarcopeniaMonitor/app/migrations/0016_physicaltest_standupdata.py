# Generated by Django 5.1.7 on 2025-04-06 15:49

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ("app", "0015_remove_questionnaire_meal_status"),
    ]

    operations = [
        migrations.AddField(
            model_name="physicaltest",
            name="standUpData",
            field=models.FloatField(blank=True, null=True),
        ),
    ]
