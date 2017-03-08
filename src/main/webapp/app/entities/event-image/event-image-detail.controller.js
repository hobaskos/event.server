(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventImageDetailController', EventImageDetailController);

    EventImageDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'DataUtils', 'entity', 'EventImage', 'EventPoll'];

    function EventImageDetailController($scope, $rootScope, $stateParams, previousState, DataUtils, entity, EventImage, EventPoll) {
        var vm = this;

        vm.eventImage = entity;
        vm.previousState = previousState.name;
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;

        var unsubscribe = $rootScope.$on('backendApp:eventImageUpdate', function(event, result) {
            vm.eventImage = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
