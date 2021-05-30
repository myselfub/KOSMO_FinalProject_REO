from reoPy.models import SearchTitle
from reoPy.models import SearchTitleCount
from rest_framework import serializers

class SearchTitleSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = SearchTitle
        fileds = ('seq', 'title', 'mem_email', 'date')

class SearchTitleCountSerializer(serializers.HyperlinkedModelSerializer):
    class Meta:
        model = SearchTitleCount
        fileds = ('title', 'mem_email')