# Generated by Django 5.1.7 on 2025-04-05 14:45

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ("app", "0006_rename_uploadedat_mealimage_uploaded_at_and_more"),
    ]

    operations = [
        migrations.AddField(
            model_name="mealimage",
            name="meal_type",
            field=models.CharField(blank=True, max_length=50, null=True),
        ),
    ]
