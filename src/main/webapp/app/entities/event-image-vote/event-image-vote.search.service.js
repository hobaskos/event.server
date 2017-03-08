(function() {
    'use strict';

    angular
        .module('backendApp')
        .factory('EventImageVoteSearch', EventImageVoteSearch);

    EventImageVoteSearch.$inject = ['$resource'];

    function EventImageVoteSearch($resource) {
        var resourceUrl =  'api/_search/event-image-votes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
