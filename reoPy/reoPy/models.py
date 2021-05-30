from django.db import models

class SearchTitle(models.Model):
    seq = models.IntegerField(default=0)
    title = models.CharField(max_length=100)
    mem_email = models.CharField(max_length=50)
    date = models.DateField

class SearchTitleCount(models.Model):
    title = models.CharField(max_length=100)
    count = models.IntegerField(default=0)