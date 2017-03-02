(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventPollDialogController', EventPollDialogController);

    EventPollDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'EventPoll', 'Event'];

    function EventPollDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, EventPoll, Event) {
        var vm = this;

        vm.eventPoll = entity;
        vm.clear = clear;
        vm.save = save;
        vm.events = Event.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.eventPoll.id !== null) {
                EventPoll.update(vm.eventPoll, onSaveSuccess, onSaveError);
            } else {
                EventPoll.save(vm.eventPoll, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('backendApp:eventPollUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
