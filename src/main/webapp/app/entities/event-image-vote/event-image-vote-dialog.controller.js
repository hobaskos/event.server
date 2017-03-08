(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventImageVoteDialogController', EventImageVoteDialogController);

    EventImageVoteDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'EventImageVote', 'User', 'EventImage'];

    function EventImageVoteDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, EventImageVote, User, EventImage) {
        var vm = this;

        vm.eventImageVote = entity;
        vm.clear = clear;
        vm.save = save;
        vm.users = User.query();
        vm.eventimages = EventImage.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.eventImageVote.id !== null) {
                EventImageVote.update(vm.eventImageVote, onSaveSuccess, onSaveError);
            } else {
                EventImageVote.save(vm.eventImageVote, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('backendApp:eventImageVoteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
